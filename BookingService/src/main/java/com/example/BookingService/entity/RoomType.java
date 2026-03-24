package com.example.BookingService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room_types", indexes = {
        @Index(name = "idx_roomtype_hotel", columnList = "hotel_id")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {

    @Id
    private String id;          // Unique Room Type ID (e.g., "RT-DELUXE-123")

    @Column(name = "hotel_id", nullable = false)
    private String hotelId;     // Reference to the Hotel Service ID

    private String name;        // e.g., "Executive Suite"

    private double basePrice;

    private int defaultCapacity;
}