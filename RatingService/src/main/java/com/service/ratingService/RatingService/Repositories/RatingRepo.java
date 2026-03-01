package com.service.ratingService.RatingService.Repositories;

import com.service.ratingService.RatingService.Dto.HotelRatingStats;
import com.service.ratingService.RatingService.Dto.RatingProjection;
import com.service.ratingService.RatingService.entities.Ratings;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;



public interface RatingRepo extends MongoRepository<Ratings, String>, CustomRatingRepo {
    // Interface projection for simple list
    List<Ratings> findByUserId(String userId);

    List<Ratings> findByHotelId(String hotelId);
    List<RatingProjection> findAllProjectedBy(Pageable pageable);
}

