package com.example.BookingService.Client;

import com.example.BookingService.Config.FeignConfig;
import com.example.BookingService.Dto.RoomTypeExportDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
@FeignClient(name = "HOTELSERVICE",configuration = FeignConfig.class)
public interface HotelClient {

    @GetMapping("/api/v1/hotels/all-room-metadata")
    List<RoomTypeExportDto> fetchAllRoomMetadata();


}
