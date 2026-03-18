package com.hotel.service.HotelService.imp;

import com.hotel.service.HotelService.Repositories.HotelRepo;
import com.hotel.service.HotelService.Repositories.RoomTypeRepo;
import com.hotel.service.HotelService.entities.Hotel;
import com.hotel.service.HotelService.entities.RoomType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final RoomTypeRepo roomTypeRepo;
    private final HotelRepo hotelRepo;

    /**
     * Add a new room type to a hotel
     */
    @Transactional
    public RoomType addRoomTypeToHotel(String hotelId, RoomType roomType) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        // Map hotel reference
        roomType.setHotel(hotel);

        // Save room type
        RoomType saved = roomTypeRepo.save(roomType);

        // Add to hotel's list (optional, keeps the object graph in sync)
        hotel.getRoomTypes().add(saved);

        return saved;
    }

    /**
     * List all room types for a hotel
     */
    public List<RoomType> getRoomTypesForHotel(String hotelId) {
        return roomTypeRepo.findByHotelId(hotelId);
    }
}
