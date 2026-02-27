package com.hotel.service.HotelService.entities;

import com.hotel.service.HotelService.Config.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Hotels", indexes = {
        // ------ index covers both the Search (name) and the Cursor (id)
        @Index(name = "idx_hotel_name_pagination", columnList = "name, id"),
        @Index(name = "idx_hotel_location", columnList = "location")
})
public class Hotel extends BaseEntity {

        @Id
        private String id ;

        private String name ;

        private String location ;


        private String contact ;

        private String about ;

        private String avgRating;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Staff> staffs = new ArrayList<>();


}
