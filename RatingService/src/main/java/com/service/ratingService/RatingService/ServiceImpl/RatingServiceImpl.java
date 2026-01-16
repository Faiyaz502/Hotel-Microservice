package com.service.ratingService.RatingService.ServiceImpl;

import com.service.ratingService.RatingService.entities.Ratings;
import com.service.ratingService.RatingService.service.RatingService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service

public class RatingServiceImpl implements RatingService {
    @Override
    public Ratings create(Ratings ratings) {
        return null;
    }

    @Override
    public List<Ratings> getAllRations() {
        return List.of();
    }

    @Override
    public List<Ratings> getRationsOfUser(String userId) {
        return List.of();
    }

    @Override
    public List<Ratings> getRationOfHotel(String hotelId) {
        return List.of();
    }
}
