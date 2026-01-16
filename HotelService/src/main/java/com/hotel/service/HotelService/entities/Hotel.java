package com.hotel.service.HotelService.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Hotels")
public class Hotel {

        @Id
        private String id ;

        private String name ;

        private String location ;


        private String contact ;

        private String about ;


}
