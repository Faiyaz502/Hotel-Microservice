package com.service.ratingService.RatingService.service;

import com.service.ratingService.RatingService.entities.Ratings;

import java.util.List;

public interface RatingService {

    //Create
    Ratings create(Ratings ratings);


    //get All

    List<Ratings> getAllRations();

    //get Userid wise

   List<Ratings> getRationsOfUser(String userId);


   //get HotelId wise

    List<Ratings> getRationOfHotel(String hotelId);




}
