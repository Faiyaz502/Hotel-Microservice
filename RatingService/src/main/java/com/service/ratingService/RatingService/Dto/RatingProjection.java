package com.service.ratingService.RatingService.Dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
@Builder
public record RatingProjection(
        @Field("_id") String ratingId, // Maps MongoDB's _id to your ratingId field
        String userId,
        String hotelId,
        int rating,
        String feedback
) implements Serializable {}
