package com.service.ratingService.RatingService.Dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RatingResponseDto implements Serializable {
    private String ratingId, userId, hotelId, feedback;
    private int rating;
}
