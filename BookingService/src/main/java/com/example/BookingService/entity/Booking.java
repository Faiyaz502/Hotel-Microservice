package com.example.BookingService.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;
    private String hotelId;
    private String roomTypeId;

    private LocalDate checkIn;
    private LocalDate checkOut;

    private String token;       // reservation token
    private String status;      // CONFIRMED, CANCELLED
    private double totalPrice;  // total price of booking
    private String paymentId;   // payment transaction ID

    private LocalDateTime createdAt;
}