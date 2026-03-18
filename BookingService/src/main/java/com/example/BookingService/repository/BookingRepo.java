package com.example.BookingService.repository;

import com.example.BookingService.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, String> {

    List<Booking> findByUserId(String userId);
    List<Booking> findByHotelIdAndRoomTypeId(String hotelId, String roomTypeId);

    // New: check for existing token
    Optional<Booking> findByToken(String token);
}