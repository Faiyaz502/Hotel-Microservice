package com.example.BookingService.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "room_inventory", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_hotel_room_date",
                columnNames = {"hotelId", "roomTypeId", "inventoryDate"}
        )
})
public class RoomInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hotelId;    // Mandatory for multi-hotel support
    private String roomTypeId;
    private LocalDate inventoryDate;

    private int totalCapacity;
    private int bookedCount;
}
