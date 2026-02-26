package com.hotel.service.HotelService.controller;

import com.hotel.service.HotelService.entities.Hotel;
import com.hotel.service.HotelService.services.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    //  CREATE =================
    @PostMapping
    public ResponseEntity<Hotel> createHotel(@RequestBody Hotel hotel){

        String hotelId = UUID.randomUUID().toString();
        hotel.setId(hotelId);

        Hotel createdHotel = hotelService.create(hotel);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdHotel);
    }

    // GET SINGLE =================
    @GetMapping("/{hotelId}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable String hotelId){

        Hotel hotel = hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotel);
    }

    // GET ALL =================
    @GetMapping
    public ResponseEntity<List<Hotel>> getAllHotels(){

        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    // UPDATE =================
    @PutMapping("/{hotelId}")
    public ResponseEntity<Hotel> updateHotel(
            @PathVariable String hotelId,
            @RequestBody Hotel hotel){

        Hotel updatedHotel = hotelService.updateHotel(hotelId, hotel);

        return ResponseEntity.ok(updatedHotel);
    }

    //  DELETE =================
    @DeleteMapping("/{hotelId}")
    public ResponseEntity<String> deleteHotel(@PathVariable String hotelId){

        hotelService.deleteHotel(hotelId);

        return ResponseEntity.ok("Hotel deleted successfully");
    }
}