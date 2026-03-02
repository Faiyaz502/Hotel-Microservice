package com.hotel.service.HotelService.Repositories;

import com.hotel.service.HotelService.Dto.StaffProjection;
import com.hotel.service.HotelService.entities.Staff;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepo extends JpaRepository<Staff, String>, JpaSpecificationExecutor<Staff> {

    // Fetching the projection directly from Postgres
    List<StaffProjection> findAllProjectedBy(Specification<Staff> spec, Pageable pageable);
}
