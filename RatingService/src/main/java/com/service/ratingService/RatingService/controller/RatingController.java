package com.service.ratingService.RatingService.controller;

import com.service.ratingService.RatingService.Dto.HotelRatingStats;
import com.service.ratingService.RatingService.Dto.PaginatedResponse;
import com.service.ratingService.RatingService.Dto.RatingProjection;
import com.service.ratingService.RatingService.entities.Ratings;
import com.service.ratingService.RatingService.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final Logger log = LoggerFactory.getLogger(RatingController.class);

    @PostMapping
    public ResponseEntity<Ratings> create(@RequestBody Ratings rating){

        log.info("Calling the POST : /ratings ->{}{},",rating.getUserId(),rating.getHotelId());

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
    //this one API can get ALL -- One Man Army

    @GetMapping
    public ResponseEntity<PaginatedResponse<RatingProjection>> getRatings(
            @RequestParam(required = false) String hotelId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) String lastId,
            @RequestParam(required = false) Integer lastRatingValue,
            @RequestParam(defaultValue = "10") int size) {

            log.info("Calling the GET : getRating /ratings api with ->{}{}{}{}{}{}",hotelId, userId, minRating, lastId, lastRatingValue, size);

        return ResponseEntity.ok(ratingService.getRatings(hotelId, userId, minRating, lastId, lastRatingValue, size));
    }


    public ResponseEntity<HotelRatingStats> getHotelStats(String hotelId){


        return ResponseEntity.status(HttpStatus.OK).body(ratingService.getStats(hotelId));


    }





}
