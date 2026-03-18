package com.example.BookingService.Dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ConfirmBookingRequest {
    private List<LocalDate> stayDates;
    private String userId;
    private double totalPrice;
    private String paymentId;
}