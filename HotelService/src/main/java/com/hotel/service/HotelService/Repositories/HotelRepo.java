package com.hotel.service.HotelService.Repositories;

import com.hotel.service.HotelService.entities.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface HotelRepo extends JpaRepository<Hotel,String> {
}
