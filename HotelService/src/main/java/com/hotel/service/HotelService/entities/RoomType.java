package com.hotel.service.HotelService.entities;


import com.hotel.service.HotelService.Config.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "room_types", indexes = {
        @Index(name = "idx_roomtype_hotel", columnList = "hotel_id")
})
public class RoomType extends BaseEntity {

    @Id
    private String id;              // Room type ID

    private String name;            // e.g., "Executive Suite"

    private double basePrice;       // Price per night

    private int defaultCapacity;    // Default capacity for this room type

    // Link back to Hotel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

}
