package com.example.BookingService.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "room_inventory", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hotel_room_date", columnNames = {"hotelId", "roomTypeId", "inventoryDate"})
})
@Data
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hotelId;
    private String roomTypeId;
    private LocalDate inventoryDate;
    private int totalCapacity;
    private int bookedCount;

    @Version
    private Integer version; // Optimistic concurrency
}