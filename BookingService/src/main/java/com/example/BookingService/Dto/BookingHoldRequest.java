package com.example.BookingService.Dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingHoldRequest {
    private String hotelId;
    private String roomTypeId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String userId;
}