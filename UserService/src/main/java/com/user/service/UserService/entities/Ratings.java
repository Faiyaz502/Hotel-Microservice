package com.user.service.UserService.entities;


import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ratings implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

        private String ratingId;

        private String userId;

        private String hotelId;

        private int rating;

        private String feedback;

        private Hotel hotel;


}
