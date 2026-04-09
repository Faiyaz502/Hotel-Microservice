package com.hotel.service.HotelService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    private List<T> content;
    private String nextCursor;
    private Double nextScore;

//    Map<String, Double> avgRatingPerLocation;
}
