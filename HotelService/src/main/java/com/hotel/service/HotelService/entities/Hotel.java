package com.hotel.service.HotelService.entities;

import com.hotel.service.HotelService.Config.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Hotels")
public class Hotel extends BaseEntity {

        @Id
        private String id ;

        private String name ;

        private String location ;


        private String contact ;

        private String about ;

        private String avgRating;


}
