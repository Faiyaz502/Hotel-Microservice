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

import java.util.Collections;
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
        public PaginatedResponse<RatingResponseDto> getRatings(
                String hotelId,
                String userId,
                Integer minRating,
                String lastId,
                Integer lastRatingValue,
                int size) {

            // ---- Limit the fetch size to prevent huge queries
            int fetchSize = Math.min(size, 100);

            // ---- Fetch projections from repository
            List<RatingProjection> projections = ratingRepo.findRatingsWithCursor(
                    hotelId, userId, minRating, lastId, lastRatingValue, fetchSize + 1);

            // ---- Handle empty result early
            if (projections.isEmpty()) {
                return new PaginatedResponse<>(Collections.emptyList(), null, null);
            }

            // ---- Map Projection -> DTO
            List<RatingResponseDto> responses = projections.stream()
                    .limit(fetchSize)
                    .map(p -> new RatingResponseDto(
                            p.ratingId(),
                            p.userId(),
                            p.hotelId(),
                            p.rating(),
                            p.feedback()
                    ))
                    .toList();

            // ---- Determine the next cursor
            String nextId = null;
            Integer nextRating = null;
            if (projections.size() > fetchSize) {
                RatingProjection last = projections.get(fetchSize); // the extra one
                nextId = last.ratingId();
                nextRating = last.rating();
            }

            log.info("Ratings fetched: hotelId={}, userId={}, size={}, hasNext={}",
                    hotelId, userId, responses.size(), nextId != null);

            return new PaginatedResponse<>(responses, nextId, nextRating);
        }


    @Override
    @Cacheable(value = "hotel_stats", key = "#hotelId")
    public HotelRatingStats getStats(String hotelId) {
        return ratingRepo.getHotelStats(hotelId);
    }



    //For external Use the main api can handle it also
    @Override
//    @Cacheable(value = "userRatings",key = "#userId")
    public List<Ratings> getRationsOfUser(String userId) {


        return ratingRepo.findByUserId(userId);
    }

    @Override
    @Cacheable(value = "hotelRatings",key = "#hotelId")
    public List<Ratings> getRationOfHotel(String hotelId) {
        return ratingRepo.findByHotelId(hotelId);
    }
}
