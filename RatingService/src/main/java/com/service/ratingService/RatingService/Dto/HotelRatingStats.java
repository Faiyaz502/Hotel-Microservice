package com.service.ratingService.RatingService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelRatingStats implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private Double averageRating;
    private Long totalRatings;
}
