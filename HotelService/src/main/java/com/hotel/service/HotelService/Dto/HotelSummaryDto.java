package com.hotel.service.HotelService.Dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotelSummaryDto implements Serializable {
    // -------- to provide a version ID for consistent serialization
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String location;
    private String avgRating;
}
