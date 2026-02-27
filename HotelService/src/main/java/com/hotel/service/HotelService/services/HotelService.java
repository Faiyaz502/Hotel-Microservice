package com.hotel.service.HotelService.services;

import com.hotel.service.HotelService.Dto.HotelProjection;
import com.hotel.service.HotelService.Dto.HotelSummaryDto;
import com.hotel.service.HotelService.Dto.PaginatedResponse;
import com.hotel.service.HotelService.entities.Hotel;

import java.util.List;

public interface HotelService {

        Hotel create(Hotel hotel);


        List<Hotel> getAllHotels();

    PaginatedResponse<HotelProjection> getHotelsPaginated(String name, String location, String lastId, int size);



        Hotel getHotelById(String id);

    Hotel updateHotel(String id, Hotel hotel);

    void deleteHotel(String id);

}
