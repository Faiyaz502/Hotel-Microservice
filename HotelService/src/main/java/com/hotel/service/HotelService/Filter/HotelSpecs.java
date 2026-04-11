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

            // NAME FILTER

            if (name != null && !name.isBlank() && !name.equalsIgnoreCase("null")) {
                String searchTerm = name.toLowerCase().trim() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchTerm));
            }

            // LOCATION FILTER

            if (location != null && !location.isBlank() && !location.equalsIgnoreCase("null")) {
                predicates.add(cb.equal(cb.lower(root.get("location")), location.toLowerCase().trim()));
            }

            //  CURSOR PAGINATION

            if (lastId != null && !lastId.isBlank() && !lastId.equalsIgnoreCase("null")) {
                predicates.add(cb.greaterThan(root.get("id"), lastId));
            }

            // SORTING CONSISTENCY

            query.orderBy(cb.asc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}