package com.hotel.service.HotelService.Repositories;

import com.hotel.service.HotelService.entities.Hotel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface HotelRepo extends JpaRepository<Hotel,String> {


    @Modifying
    @Transactional
    @Query("""
           UPDATE Hotel h SET\s
               h.name = CASE WHEN :name IS NOT NULL THEN :name ELSE h.name END,
               h.location = CASE WHEN :location IS NOT NULL THEN :location ELSE h.location END,
               h.about = CASE WHEN :about IS NOT NULL THEN :about ELSE h.about END,
               h.contact = CASE WHEN :contact IS NOT NULL THEN :contact ELSE h.contact END
           WHERE h.id = :id
          \s""")
    int updateHotelById(String id, String name, String location, String about, String contact);


}
