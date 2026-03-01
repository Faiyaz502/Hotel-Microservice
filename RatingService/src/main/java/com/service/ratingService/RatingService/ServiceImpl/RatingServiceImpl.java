package com.service.ratingService.RatingService.ServiceImpl;

import com.service.ratingService.RatingService.Dto.HotelRatingStats;
import com.service.ratingService.RatingService.Dto.PaginatedResponse;
import com.service.ratingService.RatingService.Dto.RatingProjection;
import com.service.ratingService.RatingService.Dto.RatingResponseDto;
import com.service.ratingService.RatingService.Repositories.RatingRepo;
import com.service.ratingService.RatingService.entities.Ratings;
import com.service.ratingService.RatingService.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepo ratingRepo;
    private final Logger log = LoggerFactory.getLogger(RatingServiceImpl.class);


    @Override
    @Caching(evict = {
            @CacheEvict(value = "ratings_search", allEntries = true),
            @CacheEvict(value = "userRatings", key = "#ratings.userId"),
            @CacheEvict(value = "hotelRatings", key = "#ratings.hotelId")
    })
    public Ratings create(Ratings ratings) {

        log.info("Crating a rating of ->{}{}{} ",ratings.getUserId(),ratings.getHotelId(),ratings.getRating());

        return ratingRepo.save(ratings);
    }


    //For Testing
    @Override
    @Cacheable(value = "ratings")
    public List<Ratings> getAllRations() {
        return ratingRepo.findAll();
    }

        /// Get All Ratings with optimized filter  , cursor and size
        /// can Handle millions of data
    @Override
    @Cacheable(value = "ratings_search",
            key = "{#hotelId, #userId, #minRating, #lastId, #lastRatingValue, #size}",
            unless = "#result.content.isEmpty()")
    public PaginatedResponse<RatingProjection> getRatings(
            String hotelId,
            String userId,
            Integer minRating,
            String lastId,
            Integer lastRatingValue,
            int size) {

        log.info("Calling getRatings with cursor ->{}{}{}{}{}{}",hotelId, userId, minRating, lastId, lastRatingValue, size);

        // ----Pass userId to the repo
        List<RatingProjection> projections = ratingRepo.findRatingsWithCursor(
                hotelId, userId, minRating, lastId, lastRatingValue, size);

        String nextId = null;
        Integer nextRating = null;
        List<RatingProjection> resultList;

        //Avoid the extra count query to make the system fast :D

        if (projections.size() > size) {
            RatingProjection peek = projections.get(size - 1);
            nextId = peek.getRatingId();
            nextRating = peek.getRating();
            resultList = projections.subList(0, size);
        } else {
            resultList = projections;
        }

        return new PaginatedResponse<>(resultList, nextId, nextRating);
    }


    @Override
    @Cacheable(value = "hotel_stats", key = "#hotelId")
    public HotelRatingStats getStats(String hotelId) {
        return ratingRepo.getHotelStats(hotelId);
    }



    //For external Use the main api can handle it also
    @Override
    @Cacheable(value = "userRatings",key = "#userId")
    public List<Ratings> getRationsOfUser(String userId) {
        return ratingRepo.findByUserId(userId);
    }

    @Override
    @Cacheable(value = "hotelRatings",key = "hotelId")
    public List<Ratings> getRationOfHotel(String hotelId) {
        return ratingRepo.findByHotelId(hotelId);
    }
}
