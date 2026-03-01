package com.service.ratingService.RatingService.Dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    private List<T> content;


    private String nextId;


    private Object nextSortValue;

// --------check if a next page exists

    public boolean hasNext() {
        return nextId != null;
    }
}
