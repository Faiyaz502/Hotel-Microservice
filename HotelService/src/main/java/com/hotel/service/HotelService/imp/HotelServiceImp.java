package com.hotel.service.HotelService.imp;

import com.hotel.service.HotelService.Exceptions.ResourceNotFoundException;
import com.hotel.service.HotelService.Repositories.HotelRepo;
import com.hotel.service.HotelService.entities.Hotel;
import com.hotel.service.HotelService.services.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class HotelServiceImp implements HotelService {

    private final HotelRepo hotelRepo;


    @Override
    public Hotel create(Hotel hotel) {

        return hotelRepo.save(hotel);
    }

    @Override
    public List<Hotel> getAllHotels() {
        return hotelRepo.findAll();
    }

    @Override
    public Hotel getHotelById(String id) {
        return hotelRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("Hotel Not Found"));
    }
}
