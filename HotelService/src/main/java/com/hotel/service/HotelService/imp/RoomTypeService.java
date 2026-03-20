package com.hotel.service.HotelService.imp;

import com.hotel.service.HotelService.Dto.RoomTypeExportDto;
import com.hotel.service.HotelService.Repositories.HotelRepo;
import com.hotel.service.HotelService.Repositories.RoomTypeRepo;
import com.hotel.service.HotelService.entities.Hotel;
import com.hotel.service.HotelService.entities.RoomType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final RoomTypeRepo roomTypeRepo;
    private final HotelRepo hotelRepo;

    public RoomType addRoomType(String hotelId, RoomType roomType) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        roomType.setHotel(hotel);
        // Ensure ID is generated if not provided
        if (roomType.getId() == null) {
            roomType.setId("RT-" + UUID.randomUUID().toString().substring(0, 8));
        }
        return roomTypeRepo.save(roomType);
    }

    public List<RoomType> getRoomTypesByHotel(String hotelId) {
        return roomTypeRepo.findByHotelId(hotelId);
    }

    public List<RoomTypeExportDto> findAllMetadataProjected(){


        return roomTypeRepo.findAllMetadataProjected();
    }
}
