package com.example.BookingService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "room_types")
@Data
public class RoomType {
    @Id
    private String id;
    private String hotelId; // Links to your Hotel Microservice/Table
    private String name;    // e.g., "Executive Suite"
    private double basePrice;
}
