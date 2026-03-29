package com.example.BookingService.service;

import com.example.BookingService.entity.Booking;
import com.example.BookingService.exception.RoomSoldOutException;
import com.example.BookingService.projection.InventoryProjection;
import com.example.BookingService.repository.BookingRepo;
import com.example.BookingService.repository.InventoryRepo;
import com.example.BookingService.scheduler.InventoryScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final InventoryRepo inventoryRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<String> holdRoomsScript;
    private final BookingRepo bookingRepo;
    private final Logger log = LoggerFactory.getLogger(BookingService.class);

    private static final int HOLD_TTL_SECONDS = 300;
    private static final int MAX_OPTIMISTIC_RETRIES = 3;

    /**
     * ===============================
     * PHASE 1: INITIATE HOLD (TOKEN)
     * ===============================
     */
    public String initiateHold(String hotelId,
                               String roomTypeId,
                               LocalDate checkIn,
                               LocalDate checkOut,
                               String userId,
                               String idempotencyKey) {


        String idempKey = "idemp:hold:" + idempotencyKey;

        List<LocalDate> stayDates = checkIn.datesUntil(checkOut).toList();

        if (stayDates.isEmpty()) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }

        //  Fetch inventory
        Map<LocalDate, InventoryProjection> inventoryMap =
                inventoryRepo.getInventoryBatch(hotelId, roomTypeId, stayDates);

        if (inventoryMap.size() < stayDates.size()) {
            throw new RuntimeException("Missing inventory for some dates");
        }

        //  Redis KEYS
        List<String> holdKeys = stayDates.stream()
                .map(date -> "hold:{" + hotelId + ":" + roomTypeId + "}:" + date)
                .toList();

        //  Redis ARGS (available capacity)
        List<String> args = stayDates.stream()
                .map(date -> String.valueOf(
                        inventoryMap.get(date).getTotalCapacity()
                                - inventoryMap.get(date).getBookedCount()))
                .collect(Collectors.toList());

        // Generate TOKEN
        String paymentToken = "PAY_TK_" + UUID.randomUUID();
        String tokenKey = "token:" + paymentToken;

        args.add(String.valueOf(HOLD_TTL_SECONDS));
        args.add(idempKey);
        args.add(paymentToken);
        args.add(tokenKey);

        //  Execute Lua
        try {
            String result = redisTemplate.execute(holdRoomsScript, holdKeys, args.toArray());

            if ("SOLD_OUT".equals(result)) {
                throw new RoomSoldOutException("One or more dates are sold out");
            }

            log.info("Hold created → user={} token={}", userId, result);
            return result;

        } catch (RoomSoldOutException e) {
            // Rethrow business exceptions so they aren't caught by the general 'Exception' block
            throw e;
        } catch (Exception e) {
            // This catches Redis connection issues, script syntax errors, etc.
            log.error("Redis infrastructure failure", e);
            throw new RuntimeException("Temporary system error, please retry");
        }
    }

    /**
     * =======================================
     * PHASE 2: CONFIRM BOOKING (WITH TOKEN)
     * =======================================
     */
    public Booking confirmBooking(String hotelId,
                                  String roomTypeId,
                                  List<LocalDate> stayDates,
                                  String token,
                                  String userId,
                                  double totalPrice,
                                  String paymentId) {

        int attempt = 0;
        while (attempt < MAX_OPTIMISTIC_RETRIES) {
            try {
                return performAtomicConfirmation(hotelId, roomTypeId, stayDates, token, userId, totalPrice, paymentId);
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                log.warn("Retry {}/{} due to optimistic lock", attempt, MAX_OPTIMISTIC_RETRIES);
                if (attempt >= MAX_OPTIMISTIC_RETRIES)
                    throw new RuntimeException("High traffic, retry later");
                try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

        throw new RuntimeException("Failed to confirm booking after retries");
    }

    /**
     * ===========================================
     * CORE CONFIRMATION (NO REDIS DECREMENT HERE)
     * ===========================================
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Booking performAtomicConfirmation(String hotelId,
                                             String roomTypeId,
                                             List<LocalDate> stayDates,
                                             String token,
                                             String userId,
                                             double totalPrice,
                                             String paymentId) {

        String tokenKey = "token:" + token;

        // Prevent duplicate booking
        Optional<Booking> existingBooking = bookingRepo.findByToken(token);
        if (existingBooking.isPresent()) {
            log.info("Booking already exists for token={}", token);
            return existingBooking.get();
        }

        // Validate token in Redis
        String tokenStatus = redisTemplate.opsForValue().get(tokenKey);
        if (tokenStatus == null) throw new RuntimeException("Token expired or invalid");

        // DB: Increment bookedCount atomically (optimistic lock)
        int updated = inventoryRepo.batchConfirmOptimistic(hotelId, roomTypeId, stayDates);
        if (updated != stayDates.size()) {
            throw new RuntimeException("Inventory update mismatch");
        }

        // Save booking
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setHotelId(hotelId);
        booking.setRoomTypeId(roomTypeId);
        booking.setCheckIn(stayDates.get(0));
        booking.setCheckOut(stayDates.get(stayDates.size() - 1)); // Correct checkOut
        booking.setToken(token);
        booking.setStatus("CONFIRMED");
        booking.setTotalPrice(totalPrice);
        booking.setPaymentId(paymentId);
        booking.setCreatedAt(LocalDateTime.now());

        bookingRepo.save(booking);

        // Mark token as confirmed in Redis
        redisTemplate.opsForValue().set(
                tokenKey,
                "CONFIRMED",
                Duration.ofMinutes(10)
        );

        log.info("Booking confirmed → token={}, user={}", token, userId);
        return booking;
    }
}
