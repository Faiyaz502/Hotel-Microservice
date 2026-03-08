package com.service.ratingService.RatingService.Dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record RatingResponseDto(
        String ratingId,
        String userId,
        String hotelId,
        int rating,
        String feedback
) implements Serializable {}
