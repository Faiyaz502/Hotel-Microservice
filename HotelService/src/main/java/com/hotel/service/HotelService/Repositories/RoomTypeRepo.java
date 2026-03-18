package com.hotel.service.HotelService.Repositories;


import com.hotel.service.HotelService.entities.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypeRepo extends JpaRepository<RoomType, String> {

    // Fetch all room types of a specific hotel
    List<RoomType> findByHotelId(String hotelId);


}
