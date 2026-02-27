package com.hotel.service.HotelService.Repositories;

import com.hotel.service.HotelService.Dto.HotelProjection;
import com.hotel.service.HotelService.entities.Hotel;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HotelRepo extends JpaRepository<Hotel,String>, JpaSpecificationExecutor<Hotel> {




    @Modifying
    @Transactional
    @Query("DELETE FROM Hotel h WHERE h.id = :hotelId AND :hotelId IS NOT NULL")
    int deleteByIdIfNotNull(@Param("hotelId") String hotelId);


    List<HotelProjection> findAllProjectedBy(Specification<Hotel> spec, Pageable pageable);


}
