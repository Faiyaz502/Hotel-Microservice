package com.example.BookingService.controller;

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
            @RequestParam String hotelId,
            @RequestParam String roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam String userId,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {

        String paymentToken = bookingService.initiateHold(
                hotelId, roomTypeId, checkIn, checkOut, userId, idempotencyKey);

        return ResponseEntity.ok(paymentToken);
    }

    /**
          Confirm the booking ( called by a Payment Service Webhook).
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmBooking(
            @RequestParam String hotelId,
            @RequestParam String roomTypeId,
            @RequestBody List<LocalDate> stayDates) {

        bookingService.confirmBooking(hotelId, roomTypeId, stayDates);
        return ResponseEntity.noContent().build();
    }
}
