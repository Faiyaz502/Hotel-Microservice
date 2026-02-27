package com.hotel.service.HotelService.imp;

import com.hotel.service.HotelService.Exceptions.ResourceNotFoundException;
import com.hotel.service.HotelService.Repositories.HotelRepo;
import com.hotel.service.HotelService.entities.Hotel;
import com.hotel.service.HotelService.services.HotelService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class HotelServiceImp implements HotelService {

    private final HotelRepo hotelRepo;
    private final Logger log = LoggerFactory.getLogger(HotelServiceImp.class);

    // Create-----
    @Override
    @CacheEvict(value = "hotels",allEntries = true)
    public Hotel create(Hotel hotel) {

        return hotelRepo.save(hotel);
    }

    // get All Hotels --------
    @Override
    @Cacheable(value = "hotels")
    public List<Hotel> getAllHotels() {

        log.info("------Getting All Hotels from DB--------");
        return hotelRepo.findAll();
    }

    @Override
    @Cacheable(value = "hotel",key = "#hotelId")
    public Hotel getHotelById(String hotelId) {

        log.info("------Getting  Hotel from DB- ID : ->{}",hotelId);
        return hotelRepo.findById(hotelId).orElseThrow(()-> new ResourceNotFoundException("Hotel Not Found"));
    }


    // -----UPDATE--
    @Override
    @CacheEvict(value = "hotels", allEntries = true)
    @CachePut(value = "hotel", key = "#id")
    public Hotel updateHotel(String id, Hotel hotel) {

        log.info("------UPDATING Hotel ON DB : ID ------- ->{}",id);

        // -------- JPQL update
        int updatedRows = hotelRepo.updateHotelById(
                id,
                hotel.getName(),
                hotel.getLocation(),
                hotel.getAbout(),
                hotel.getContact()
        );



        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Hotel Not Found With Id: " + id);
        }

        log.info("------UPDATED Hotel ON DB : ID ------- ->{}",id);

        // Manually construct the updated Hotel object for cache reduce extra DB hit---
        Hotel cachedHotel = new Hotel();
        cachedHotel.setId(id);
        cachedHotel.setName(hotel.getName());
        cachedHotel.setLocation(hotel.getLocation());
        cachedHotel.setAbout(hotel.getAbout());
        cachedHotel.setContact(hotel.getContact());


        log.info("------UPDATING Hotel ON Redis Caching Layer : ID ------- ->{}",id);

        return cachedHotel; // this goes directly into @CachePut
    }

    // -----DELETE
    @Override
    @Caching(evict = {
            @CacheEvict(value = "hotel", key = "#hotelId"),
            @CacheEvict(value = "hotels", allEntries = true)
    })
    public void deleteHotel(String hotelId) {
        int deletedCount = hotelRepo.deleteByIdIfNotNull(hotelId);
        if (deletedCount == 0) {
            // No hotel deleted, either null or invalid ID
            throw new ResourceNotFoundException("Hotel not found or invalid ID");
        }
    }
}
