package com.hotel.service.HotelService.imp;

import com.hotel.service.HotelService.Dto.PaginatedResponse;
import com.hotel.service.HotelService.Dto.StaffProjection;
import com.hotel.service.HotelService.Repositories.StaffRepo;
import com.hotel.service.HotelService.entities.Staff;
import com.hotel.service.HotelService.services.StuffService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StuffService {

    private final StaffRepo staffRepo;

    @Override
    @Cacheable(value = "staff_search", key = "{#hotelId, #role, #lastId, #size}")
    public PaginatedResponse<StaffProjection> getStaffPaginated(String hotelId, String role, String lastId, int size) {

        //--- Create Specification for filtering and cursor
        Specification<Staff> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hotelId != null) predicates.add(cb.equal(root.get("hotelId"), hotelId));
            if (role != null) predicates.add(cb.equal(root.get("role"), role));

            // Cursor: ID-based pagination (Assuming ID is unique and sortable)
            if (lastId != null) {
                predicates.add(cb.greaterThan(root.get("id"), lastId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // --- Fetch Size + 1
        Pageable pageable = PageRequest.of(0, size + 1, Sort.by("id").ascending());
        List<StaffProjection> projections = staffRepo.findAllProjectedBy(spec, pageable);

        String nextId = null;
        List<StaffProjection> resultList;

        // ---- Peek Logic
        if (projections.size() > size) {
            nextId = projections.get(size - 1).getId();
            resultList = projections.subList(0, size);
        } else {
            resultList = projections;
        }

        return new PaginatedResponse<>(resultList, nextId);
    }
}
