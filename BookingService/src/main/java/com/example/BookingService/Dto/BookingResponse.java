package com.example.BookingService.Dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingResponse {
    private String bookingId;
    private String token;
    private String status; // CONFIRMED, CANCELLED
    private double totalPrice;
    private String paymentId;
    private LocalDate checkIn;
    private LocalDate checkOut;
}