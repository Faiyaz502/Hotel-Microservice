package com.example.BookingService.controller;

import com.example.BookingService.Dto.BookingHoldRequest;
import com.example.BookingService.Dto.BookingResponse;
import com.example.BookingService.Dto.ConfirmBookingRequest;
import com.example.BookingService.entity.Booking;
import com.example.BookingService.scheduler.InventoryScheduler;
import com.example.BookingService.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;


    /**
         Place a temporary hold on rooms.
     */
    @PostMapping("/hold")
    public ResponseEntity<String> initiateHold(
            @RequestBody BookingHoldRequest request,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {



        String paymentToken = bookingService.initiateHold(
                request.getHotelId(),
                request.getRoomTypeId(),
                request.getCheckIn(),
                request.getCheckOut(),
                request.getUserId(),
                idempotencyKey);

        return ResponseEntity.ok(paymentToken);
    }

    /**
          Confirm the booking ( called by a Payment Service Webhook).
     */

    @PostMapping("/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @RequestParam String hotelId,
            @RequestParam String roomTypeId,
            @RequestParam String token,
            @RequestBody ConfirmBookingRequest request) {

        Booking booking = bookingService.confirmBooking(
                hotelId,
                roomTypeId,
                request.getStayDates(),
                token,
                request.getUserId(),
                request.getTotalPrice(),
                request.getPaymentId()
        );

        // Build response
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setToken(booking.getToken());
        response.setStatus(booking.getStatus());
        response.setTotalPrice(booking.getTotalPrice());
        response.setPaymentId(booking.getPaymentId());
        response.setCheckIn(booking.getCheckIn());
        response.setCheckOut(booking.getCheckOut());

        return ResponseEntity.ok(response);
    }
}
