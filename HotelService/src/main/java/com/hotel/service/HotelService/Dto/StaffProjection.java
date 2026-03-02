package com.hotel.service.HotelService.Dto;

import java.io.Serializable;

public interface StaffProjection extends Serializable {
    String getId();
    String getName();
    String getRole();
    String getHotelId();
}
