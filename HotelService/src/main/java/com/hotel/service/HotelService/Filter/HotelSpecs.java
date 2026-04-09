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

            // 1. NAME FILTER (Case-Insensitive with Trailing Wildcard)
            // Added check for literal string "null" from Frontend/URL params
            if (name != null && !name.isBlank() && !name.equalsIgnoreCase("null")) {
                String searchTerm = name.toLowerCase().trim() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchTerm));
            }

            // 2. LOCATION FILTER (Case-Insensitive)
            // Changed from .equal() to case-insensitive matching for better reliability
            if (location != null && !location.isBlank() && !location.equalsIgnoreCase("null")) {
                predicates.add(cb.equal(cb.lower(root.get("location")), location.toLowerCase().trim()));
            }

            // 3. CURSOR PAGINATION
            // Ensure lastId is actually a valid ID before applying the filter
            if (lastId != null && !lastId.isBlank() && !lastId.equalsIgnoreCase("null")) {
                predicates.add(cb.greaterThan(root.get("id"), lastId));
            }

            // 4. SORTING CONSISTENCY
            // Explicitly order by ID to ensure Cursor Pagination works correctly
            query.orderBy(cb.asc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}