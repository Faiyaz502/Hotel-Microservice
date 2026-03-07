package com.hotel.service.HotelService.Dto;

import java.io.Serializable;

public record HotelResponse(String id, String name, String location, String avgRating) implements Serializable {}
