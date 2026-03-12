package com.user.service.UserService.externalService;

import com.user.service.UserService.Interceptor.FeignConfig;
import com.user.service.UserService.Payload.HotelSummaryDto;
import com.user.service.UserService.entities.Hotel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "HOTELSERVICE", configuration = FeignConfig.class)
public interface HotelService {

    @GetMapping("/api/v1/hotels/{hotelId}")
        // Change Hotel to HotelSummaryDto to match the Controller's return type
    ResponseEntity<HotelSummaryDto> getHotel(@PathVariable("hotelId") String hotelId);
}
