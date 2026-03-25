package com.hotel.service.HotelService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomTypeExportDto {
    private String roomTypeId;
    private String hotelId;
    private String roomName;
    private double basePrice;
    private int defaultCapacity;

}