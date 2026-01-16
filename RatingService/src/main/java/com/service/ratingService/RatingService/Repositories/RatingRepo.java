package com.service.ratingService.RatingService.Repositories;

import com.service.ratingService.RatingService.entities.Ratings;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RatingRepo extends MongoRepository<Ratings,String> {

}
