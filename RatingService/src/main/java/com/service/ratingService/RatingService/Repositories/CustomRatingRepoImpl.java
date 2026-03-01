package com.service.ratingService.RatingService.Repositories;

import com.service.ratingService.RatingService.Dto.HotelRatingStats;
import com.service.ratingService.RatingService.Dto.RatingProjection;
import com.service.ratingService.RatingService.entities.Ratings;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomRatingRepoImpl implements CustomRatingRepo {
    private final MongoTemplate mongoTemplate;
    private final Logger log = LoggerFactory.getLogger(CustomRatingRepoImpl.class);

    @Override
    public List<RatingProjection> findRatingsWithCursor(
            String hotelId,
            String userId,
            Integer minRating,
            String lastId,
            Integer lastRatingValue,
            int size) {

        log.info("Finding Rating findRatingsWithCursor in CustomerRatingImp with ->{}{}{}{}{}{}",hotelId,userId,minRating,lastId,lastRatingValue,size);

        Query query = new Query();

        // ------ Dynamic Filtering -- where clause
        if (hotelId != null) query.addCriteria(Criteria.where("hotelId").is(hotelId));
        if (userId != null) query.addCriteria(Criteria.where("userId").is(userId));
        if (minRating != null) query.addCriteria(Criteria.where("rating").gte(minRating));

        //--- Composite Cursor Logic === unchanged, still uses rating + _id ====
        if (lastId != null && lastRatingValue != null) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("rating").lt(lastRatingValue),
                    Criteria.where("rating").is(lastRatingValue).and("_id").gt(new ObjectId(lastId))
            ));
        }

        query.limit(size + 1);
        query.with(Sort.by(Sort.Order.desc("rating"), Sort.Order.asc("_id")));

        // -----Field Projection (including feedback as requested earlier)
        query.fields().include("ratingId", "userId", "hotelId", "rating", "feedback");


        return mongoTemplate.find(query, RatingProjection.class, "user_ratings");
    }

    @Override
    public HotelRatingStats getHotelStats(String hotelId) {
        log.info("Calling getHotelStats for Hotel ID: ->{}",hotelId);

        MatchOperation match = Aggregation.match(Criteria.where("hotelId").is(hotelId));
        GroupOperation group = Aggregation.group("hotelId")
                .avg("rating").as("averageRating")
                .count().as("totalRatings");

        return mongoTemplate.aggregate(Aggregation.newAggregation(match, group),
                "user_ratings", HotelRatingStats.class).getUniqueMappedResult();
    }
}
