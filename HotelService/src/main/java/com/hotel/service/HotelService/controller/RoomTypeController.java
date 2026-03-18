package com.hotel.service.HotelService.controller;

import com.hotel.service.HotelService.entities.RoomType;

import com.hotel.service.HotelService.imp.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hotels/{hotelId}/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    /**
     * Add a new room type to a specific hotel
     */
    @PostMapping
    public ResponseEntity<RoomType> addRoomType(
            @PathVariable String hotelId,
            @RequestBody RoomType roomType) {

        RoomType saved = roomTypeService.addRoomTypeToHotel(hotelId, roomType);
        return ResponseEntity.ok(saved);
    }

    /**
     * List all room types of a hotel
     */
    @GetMapping
    public ResponseEntity<List<RoomType>> getRoomTypes(@PathVariable String hotelId) {
        List<RoomType> roomTypes = roomTypeService.getRoomTypesForHotel(hotelId);
        return ResponseEntity.ok(roomTypes);
    }
}
