package com.service.ratingService.RatingService.Dto;

import java.io.Serializable;

public interface RatingProjection extends Serializable {
    String getRatingId();
    String getUserId();
    String getHotelId();
    int getRating();
    String getFeedback();
}
