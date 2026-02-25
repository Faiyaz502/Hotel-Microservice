package com.service.ratingService.RatingService.ServiceImpl;

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
            @CacheEvict(value = "ratings", allEntries = true),
            @CacheEvict(value = "userRatings", key = "#ratings.userId"),
            @CacheEvict(value = "hotelRatings", key = "#ratings.hotelId")
    })
    public Ratings create(Ratings ratings) {

        return ratingRepo.save(ratings);
    }

    @Override
    @Cacheable(value = "ratings")
    public List<Ratings> getAllRations() {
        return ratingRepo.findAll();
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
