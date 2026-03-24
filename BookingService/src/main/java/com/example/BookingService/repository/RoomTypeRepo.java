package com.example.BookingService.repository;

import com.example.BookingService.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypeRepo extends JpaRepository<RoomType, String> {

    // Optional: fetch all room types
    List<RoomType> findAll();

    @Query("SELECT r.id FROM RoomType r WHERE r.id IN :ids")
    List<String> findExistingRoomTypeIds(@Param("ids") List<String> ids);
}
