package com.hotel.service.HotelService.controller;

import com.hotel.service.HotelService.Dto.RoomTypeExportDto;
import com.hotel.service.HotelService.entities.RoomType;

import com.hotel.service.HotelService.imp.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
public class HotelRoomController {

    private final RoomTypeService roomTypeService;

    // 1. Add Room Type to a specific Hotel
    @PostMapping("/{hotelId}/rooms")
    public ResponseEntity<RoomType> addRoomToHotel(
            @PathVariable String hotelId,
            @RequestBody RoomType roomType) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomTypeService.addRoomType(hotelId, roomType));
    }

    // 2. Export API for Booking Service (Internal Communication)

    @GetMapping("/all-room-metadata")
    public ResponseEntity<List<RoomTypeExportDto>> getAllMetadata() {
        // 🚀 High-Performance Projection: Direct DB to DTO mapping
        List<RoomTypeExportDto> dtos = roomTypeService.findAllMetadataProjected();

        return ResponseEntity.ok(dtos);
    }
}
