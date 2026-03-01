package com.service.ratingService.RatingService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelRatingStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;             // MongoDB maps the group key (hotelId) to 'id'
    private Double averageRating;  // Matches .avg("rating").as("averageRating")
    private Long totalRatings;     // Matches .count().as("totalRatings")
}
