package com.hotel.service.HotelService.Dto;

import java.io.Serializable;

public interface HotelProjection extends Serializable {
    String getId();
    String getName();
    String getLocation();
    String getAvgRating();
}
