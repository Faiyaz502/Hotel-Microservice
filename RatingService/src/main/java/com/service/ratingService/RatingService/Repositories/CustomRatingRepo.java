package com.service.ratingService.RatingService.Repositories;

import com.service.ratingService.RatingService.Dto.HotelRatingStats;
import com.service.ratingService.RatingService.Dto.RatingProjection;
import com.service.ratingService.RatingService.entities.Ratings;

import java.util.List;

public interface CustomRatingRepo {
    List<RatingProjection> findRatingsWithCursor(String hotelId, Integer minRating, String lastId, Integer lastRatingValue, int size);
    HotelRatingStats getHotelStats(String hotelId);
}
