package com.hotel.service.HotelService.Filter;

import com.hotel.service.HotelService.entities.Hotel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;


import java.util.ArrayList;
import java.util.List;

public class HotelSpecs {
    public static Specification<Hotel> getSpec(String name, String location, String lastId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // SEARCH OPTIMIZATION: Used trailing wildcard only

            if (name != null && !name.isBlank()) {
                // query + "%" ensures the index is utilized
                predicates.add(cb.like(cb.lower(root.get("name")), name.toLowerCase() + "%"));
            }

            if (location != null && !location.isBlank()) {
                predicates.add(cb.equal(root.get("location"), location));
            }

            // CURSOR PAGINATION
            if (lastId != null && !lastId.isBlank()) {
                predicates.add(cb.greaterThan(root.get("id"), lastId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
