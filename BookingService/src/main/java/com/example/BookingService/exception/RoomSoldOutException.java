package com.example.BookingService.exception;

public class RoomSoldOutException extends RuntimeException {
    public RoomSoldOutException(String message) {
        super(message);
    }
}
