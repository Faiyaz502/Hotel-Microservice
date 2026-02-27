package com.hotel.service.HotelService.controller;

import com.hotel.service.HotelService.entities.Hotel;
import com.hotel.service.HotelService.services.HotelService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger log = LoggerFactory.getLogger(HotelController.class);

    //  CREATE =================
    @PostMapping
    public ResponseEntity<Hotel> createHotel(@RequestBody Hotel hotel){

        log.info("POST :Create Hotel /hotels Calling ->{}",hotel);

        String hotelId = UUID.randomUUID().toString();
        hotel.setId(hotelId);

        Hotel createdHotel = hotelService.create(hotel);

        log.info("Created Hotel /hotels by Calling ->{}",createdHotel);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdHotel);
    }

    // GET SINGLE =================
    @GetMapping("/{hotelId}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable String hotelId){



        Hotel hotel = hotelService.getHotelById(hotelId);

        log.info("Getting Hotel /hotels/hotelId Calling Hotel :->{}",hotel);

        return ResponseEntity.ok(hotel);
    }

    // GET ALL =================
    @GetMapping
    public ResponseEntity<List<Hotel>> getAllHotels(){

        log.info("GET : Getting ALLHotel /hotels Calling ID");

        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    // UPDATE =================
    @PutMapping("/{hotelId}")
    public ResponseEntity<Hotel> updateHotel(
            @PathVariable String hotelId,
            @RequestBody Hotel hotel){
        log.info("PUT : Hotel /hotels/hotelId Calling ID :->{}",hotelId);

        Hotel updatedHotel = hotelService.updateHotel(hotelId, hotel);

        log.info("PUT : Hotel /hotels/hotelId Calling ID :->{}",updatedHotel);

        return ResponseEntity.ok(updatedHotel);
    }

    //  DELETE =================
    @DeleteMapping("/{hotelId}")
    public ResponseEntity<String> deleteHotel(@PathVariable String hotelId){

        log.info("DELETE : Hotel /hotels/hotelId Calling ID :->{}",hotelId);

        hotelService.deleteHotel(hotelId);

        log.info("DELETE : Hotel /hotels/hotelId HOTEL DELETED ID :->{}",hotelId);

        return ResponseEntity.ok("Hotel deleted successfully");
    }
}