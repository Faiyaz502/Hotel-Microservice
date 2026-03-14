package com.example.BookingService.repository;

import com.example.BookingService.entity.RoomInventory;
import com.example.BookingService.projection.InventoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface InventoryRepo extends JpaRepository<RoomInventory, Long> {

    @Query("SELECT r.inventoryDate as inventoryDate, r.totalCapacity as totalCapacity, r.bookedCount as bookedCount " +
            "FROM RoomInventory r WHERE r.hotelId = :hotelId AND r.roomTypeId = :roomTypeId AND r.inventoryDate IN :dates")
    Map<LocalDate, InventoryProjection> getInventoryBatch(String hotelId, String roomTypeId, List<LocalDate> dates);

    @Modifying
    @Query("UPDATE RoomInventory r SET r.bookedCount = r.bookedCount + 1 " +
            "WHERE r.hotelId = :hotelId AND r.roomTypeId = :roomTypeId AND r.inventoryDate IN :dates")
    int batchConfirm(String hotelId, String roomTypeId, List<LocalDate> dates);

    @Modifying
    @Query("UPDATE RoomInventory r SET r.bookedCount = r.bookedCount + 1 " +
            "WHERE r.hotelId = :hotelId AND r.roomTypeId = :roomTypeId AND r.inventoryDate IN :dates " +
            "AND r.version = :version")
    int batchConfirmOptimistic(String hotelId, String roomTypeId, List<LocalDate> dates);
}
