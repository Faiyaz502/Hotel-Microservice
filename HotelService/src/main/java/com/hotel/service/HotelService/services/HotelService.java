package com.hotel.service.HotelService.services;

import com.hotel.service.HotelService.entities.Hotel;

import java.util.List;

public interface HotelService {

        Hotel create(Hotel hotel);


        List<Hotel> getAllHotels();



        Hotel getHotelById(String id);



}
