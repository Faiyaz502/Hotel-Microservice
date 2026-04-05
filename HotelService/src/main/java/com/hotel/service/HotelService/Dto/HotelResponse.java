package com.hotel.service.HotelService.Dto;

import java.io.Serializable;

public record HotelResponse(String id, String name, String location, Double avgRating,Double lastScore) implements Serializable {}
