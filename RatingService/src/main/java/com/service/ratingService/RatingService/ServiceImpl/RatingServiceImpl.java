package com.service.ratingService.RatingService.ServiceImpl;

import com.service.ratingService.RatingService.Repositories.RatingRepo;
import com.service.ratingService.RatingService.entities.Ratings;
import com.service.ratingService.RatingService.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepo ratingRepo;


    @Override
    public Ratings create(Ratings ratings) {

        return ratingRepo.save(ratings);
    }

    @Override
    public List<Ratings> getAllRations() {
        return ratingRepo.findAll();
    }

    @Override
    public List<Ratings> getRationsOfUser(String userId) {
        return ratingRepo.findByUserId(userId);
    }

    @Override
    public List<Ratings> getRationOfHotel(String hotelId) {
        return ratingRepo.findByHotelId(hotelId);
    }
}
