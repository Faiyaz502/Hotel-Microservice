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
@Table(name = "Staff")
public class Staff extends BaseEntity {

    @Id
    private String id;

    private String name;

    private String email;

    private String contact;

    private String position; //--- Manager, Receptionist, Chef

    private String about;

    private String shift; // ----- Morning, Evening

    // Many staff can belong to one hotel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

}
