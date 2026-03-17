package com.example.BookingService.service;

import com.example.BookingService.projection.InventoryProjection;
import com.example.BookingService.repository.InventoryRepo;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final InventoryRepo inventoryRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<String> holdRoomsScript;

    private static final int HOLD_TTL_SECONDS = 300; // 5-min payment window
    private static final int MAX_OPTIMISTIC_RETRIES = 3;
    /**
     * Initiate Hold (Phase 1)
     */
    public String initiateHold(String hotelId,
                               String roomTypeId,
                               LocalDate checkIn,
                               LocalDate checkOut,
                               String userId,
                               String idempotencyKey) {

        String idempKey = "idemp:hold:" + idempotencyKey;
        List<LocalDate> stayDates = checkIn.datesUntil(checkOut).toList();
        if (stayDates.isEmpty()) throw new IllegalArgumentException("Check-out must be after check-in");

        // Batch fetch inventory from DB
        Map<LocalDate, InventoryProjection> inventoryMap = inventoryRepo.getInventoryBatch(hotelId, roomTypeId, stayDates);

        if (inventoryMap.size() < stayDates.size()) {
            throw new RuntimeException("Missing inventory for some dates");
        }

        // Prepare Redis keys
        List<String> holdKeys = stayDates.stream()
                .map(date -> "hold:{" + hotelId + ":" + roomTypeId + "}:" + date)
                .toList();

        // Prepare Lua arguments (capacity)
        List<String> args = stayDates.stream()
                .map(date -> String.valueOf(inventoryMap.get(date).getTotalCapacity() - inventoryMap.get(date).getBookedCount()))
                .collect(Collectors.toList());

        // Add TTL, idempotency, token
        String paymentToken = "PAY_TK_" + UUID.randomUUID();
        args.add(String.valueOf(HOLD_TTL_SECONDS));
        args.add(idempKey);
        args.add(paymentToken);

        // Execute Lua script atomically
        try {
            String result = redisTemplate.execute(holdRoomsScript, holdKeys, args.toArray());
            if ("SOLD_OUT".equals(result)) {
                log.warn("Booking failed: sold out for dates {}-{}", checkIn, checkOut);
                throw new RuntimeException("One or more dates are sold out");
            }
            log.info("Hold created for user {} token {}", userId, result);
            return result;
        } catch (Exception e) {
            log.error("Redis failure during hold creation", e);
            throw new RuntimeException("Temporary error, please retry later");
        }
    }

    // --- Inside BookingService.java ---

    /**
     * Phase 2: Confirm Booking (External Wrapper)
     * Handles the retry logic OUTSIDE the transaction.
     */
    public void confirmBooking(String hotelId, String roomTypeId, List<LocalDate> stayDates) {
        int attempt = 0;
        while (attempt < MAX_OPTIMISTIC_RETRIES) {
            try {
                // Call the transactional method
                performAtomicConfirmation(hotelId, roomTypeId, stayDates);

                return; // Success!
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                log.warn("Optimistic lock failure. Attempt {}/{} for {}-{}",
                        attempt, MAX_OPTIMISTIC_RETRIES, hotelId, roomTypeId);

                if (attempt >= MAX_OPTIMISTIC_RETRIES) {
                    throw new RuntimeException("High traffic detected. Please try confirming again.");
                }

                // Optional: Small backoff before retrying
                try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }

    /**
     * The actual DB and Redis update.
     * Marked as REQUIRES_NEW to ensure a fresh transaction for every retry.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void performAtomicConfirmation(String hotelId, String roomTypeId, List<LocalDate> stayDates) {
        //  Update DB (The source of truth)
        int updated = inventoryRepo.batchConfirmOptimistic(hotelId, roomTypeId, stayDates);

        if (updated != stayDates.size()) {
            throw new RuntimeException("DB update mismatch: expected " + stayDates.size() + " but got " + updated);
        }

        //  Decrement Redis holds
        // If this fails, the DB is still committed, and Redis will expire in 5 mins anyway (Safety Net)
        try {
            redisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                for (LocalDate date : stayDates) {
                    String key = "hold:{" + hotelId + ":" + roomTypeId + "}:" + date;
                    connection.decr(key.getBytes());
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Redis decrement failed after DB commit. Hold will leak for 5 mins.", e);
        }
    }
}
