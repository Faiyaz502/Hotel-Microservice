package com.hotel.service.HotelService.services;

import com.hotel.service.HotelService.Dto.PaginatedResponse;
import com.hotel.service.HotelService.Dto.StaffProjection;

public interface StuffService {

    PaginatedResponse<StaffProjection> getStaffPaginated(String hotelId, String role, String lastId, int size);
}
