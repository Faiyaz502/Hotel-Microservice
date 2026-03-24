package com.example.BookingService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "room_inventory", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hotel_room_date", columnNames = {"hotel_id", "room_type_id", "inventory_date"})
}, indexes = {
        // High-performance index for Phase 1 & 2 availability lookups
        @Index(name = "idx_inv_lookup", columnList = "hotel_id, room_type_id, inventory_date")
})
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private String hotelId;

    // Linked to your local RoomType cache
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "inventory_date", nullable = false)
    private LocalDate inventoryDate;

    private int totalCapacity;

    private int bookedCount;

    @Version
    private Integer version; // Core for preventing overbooking (Optimistic Locking)
}