package com.service.ratingService.RatingService.entities;

import com.service.ratingService.RatingService.Config.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document("user_ratings")
@CompoundIndex(name = "idx_hotel_rating_pagination", def = "{'hotelId': 1, 'rating': -1, '_id': 1}")
public class Ratings extends BaseEntity {

    @Id
    private String ratingId;

    private String userId;

    private String hotelId;

    private int rating;

    private String feedback;



}
