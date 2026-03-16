package com.example.BookingService.repository;

import com.example.BookingService.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypeRepo extends JpaRepository<RoomType, String> {

    // Fetch all room types for active hotels
    List<RoomType> findByHotelId(String hotelId);

    // Optional: fetch all room types
    List<RoomType> findAll();
}
