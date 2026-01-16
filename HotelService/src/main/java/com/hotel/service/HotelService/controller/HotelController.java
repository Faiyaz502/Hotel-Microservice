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

    //create

    @PostMapping
    public ResponseEntity<Hotel> createHotel(@RequestBody Hotel hotel){

        String hotelId = UUID.randomUUID().toString();

        hotel.setId(hotelId);


        Hotel createdHotel = hotelService.create(hotel);




        return ResponseEntity.status(HttpStatus.CREATED).body(createdHotel);


    }


    /// Get Single

    @GetMapping("/{hotelId}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable String hotelId){


        return ResponseEntity.status(HttpStatus.OK).body(hotelService.getHotelById(hotelId));

    }

    //get All


    @GetMapping
    public ResponseEntity<List<Hotel>> getAllHotels(){

        return ResponseEntity.ok(hotelService.getAllHotels());


    }




}
