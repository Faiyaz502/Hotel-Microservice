package com.service.ratingService.RatingService.controller;

import com.service.ratingService.RatingService.Dto.PaginatedResponse;
import com.service.ratingService.RatingService.Dto.RatingProjection;
import com.service.ratingService.RatingService.entities.Ratings;
import com.service.ratingService.RatingService.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<Ratings> create(@RequestBody Ratings rating){

        Ratings createdRating = ratingService.create(rating);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdRating);


    }

//    @GetMapping
//    public ResponseEntity<List<Ratings>> getAllRatings(){
//
//
//        return ResponseEntity.status(HttpStatus.OK).body(ratingService.getAllRations());
//
//    }

    //----- SEARCH & PAGINATED LIST----- (The Million-Row Optimized Endpoint) ------
    @GetMapping
    public ResponseEntity<PaginatedResponse<RatingProjection>> getRatings(
            @RequestParam(required = false) String hotelId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) String lastId,          // Cursor: ID
            @RequestParam(required = false) Integer lastRatingValue, // Cursor: Rating value
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ratingService.getRatings(hotelId, minRating, lastId, lastRatingValue, size));
    }


    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Ratings>> getRatingByUserId(@PathVariable String userId){


        return ResponseEntity.status(HttpStatus.OK).body(ratingService.getRationsOfUser(userId));

    }


    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<List<Ratings>> getRatingByHotelId(@PathVariable String hotelId){


        return ResponseEntity.status(HttpStatus.OK).body(ratingService.getRationOfHotel(hotelId));

    }








}
