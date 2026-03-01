package com.service.ratingService.RatingService.ServiceImpl;

import com.service.ratingService.RatingService.Dto.HotelRatingStats;
import com.service.ratingService.RatingService.Dto.PaginatedResponse;
import com.service.ratingService.RatingService.Dto.RatingProjection;
import com.service.ratingService.RatingService.Dto.RatingResponseDto;
import com.service.ratingService.RatingService.Repositories.RatingRepo;
import com.service.ratingService.RatingService.entities.Ratings;
import com.service.ratingService.RatingService.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepo ratingRepo;


    @Override
    @Caching(evict = {
            @CacheEvict(value = "ratings_search", allEntries = true),
            @CacheEvict(value = "userRatings", key = "#ratings.userId"),
            @CacheEvict(value = "hotelRatings", key = "#ratings.hotelId")
    })
    public Ratings create(Ratings ratings) {

        return ratingRepo.save(ratings);
    }


    //For Testing
    @Override
    @Cacheable(value = "ratings")
    public List<Ratings> getAllRations() {
        return ratingRepo.findAll();
    }


    @Override
    @Cacheable(value = "ratings_search",
            key = "{#hotelId, #minRating, #lastId, #lastRatingValue, #size}",
            unless = "#result.content.isEmpty()")
    public PaginatedResponse<RatingProjection> getRatings(String hotelId, Integer minRating, String lastId, Integer lastRatingValue, int size) {

        //--- Fetch projections (size + 1)
        List<RatingProjection> projections = ratingRepo.findRatingsWithCursor(hotelId, minRating, lastId, lastRatingValue, size);

        String nextId = null;
        Integer nextRating = null;
        List<RatingProjection> resultList;

        // --Logic to determine if there is a next page
        if (projections.size() > size) {
            // Peek at the last item of the current page (index size-1)
            RatingProjection peek = projections.get(size - 1);
            nextId = peek.getRatingId();
            nextRating = peek.getRating();

            // Return exactly the requested amount
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
