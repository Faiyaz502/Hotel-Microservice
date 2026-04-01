package com.example.NotifationService.payload;

import lombok.Data;

@Data
public class BookingConfirmationEvent {
    private String bookingId;
    private String userId;
    private String hotelId;
    private String status;
}
