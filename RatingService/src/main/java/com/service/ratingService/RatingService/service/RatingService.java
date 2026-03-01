package com.service.ratingService.RatingService.service;

import com.service.ratingService.RatingService.Dto.HotelRatingStats;
import com.service.ratingService.RatingService.Dto.PaginatedResponse;
import com.service.ratingService.RatingService.Dto.RatingProjection;
import com.service.ratingService.RatingService.Dto.RatingResponseDto;
import com.service.ratingService.RatingService.entities.Ratings;

import java.util.List;

public interface RatingService {

    //Create
    Ratings create(Ratings ratings);


    //get All With Pagination

    PaginatedResponse<RatingProjection>  getRatings(String hotelId,  String userId, Integer minRating, String lastId, Integer lastRatingValue, int size);

    //Get stats
    HotelRatingStats getStats(String hotelId);

    //get All

    List<Ratings> getAllRations();

    //get Userid wise

   List<Ratings> getRationsOfUser(String userId);


   //get HotelId wise

    List<Ratings> getRationOfHotel(String hotelId);




}
