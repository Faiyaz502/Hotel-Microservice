package com.example.BookingService.repository;

import com.example.BookingService.entity.RoomInventory;
import com.example.BookingService.projection.InventoryKeyProjection;
import com.example.BookingService.projection.InventoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface InventoryRepo extends JpaRepository<RoomInventory, Long> {

    //  Fetch inventory for multiple dates
    @Query("SELECT r.inventoryDate as inventoryDate, r.totalCapacity as totalCapacity, r.bookedCount as bookedCount " +
            "FROM RoomInventory r WHERE r.hotelId = :hotelId AND r.roomType.id = :roomTypeId AND r.inventoryDate IN :dates")
    List<InventoryProjection> getInventoryBatchRaw(@Param("hotelId") String hotelId,
                                                   @Param("roomTypeId") String roomTypeId,
                                                   @Param("dates") List<LocalDate> dates);

    default Map<LocalDate, InventoryProjection> getInventoryBatch(String hotelId, String roomTypeId, List<LocalDate> dates) {
        return getInventoryBatchRaw(hotelId, roomTypeId, dates)
                .stream()
                .collect(Collectors.toMap(InventoryProjection::getInventoryDate, i -> i));
    }



    //  Optimistic lock per date
    default int batchConfirmOptimistic(String hotelId, String roomTypeId, List<LocalDate> dates) {
        int updated = 0;

        for (LocalDate date : dates) {
            RoomInventory ri = findByHotelIdAndRoomTypeIdAndInventoryDate(hotelId, roomTypeId, date);
            if (ri == null) throw new RuntimeException("Inventory missing for " + date);

            // Increment bookedCount, Hibernate will check @Version automatically
            ri.setBookedCount(ri.getBookedCount() + 1);
            save(ri); // Will throw OptimisticLockException if version mismatch
            updated++;
        }
        return updated;
    }

    //  Check existence
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM RoomInventory r " +
            "WHERE r.hotelId = :hotelId AND r.roomType.id = :roomTypeId AND r.inventoryDate = :date")
    boolean existsByHotelAndRoomTypeAndDate(@Param("hotelId") String hotelId,
                                            @Param("roomTypeId") String roomTypeId,
                                            @Param("date") LocalDate date);

    //  Helper to load single row for optimistic lock
    RoomInventory findByHotelIdAndRoomTypeIdAndInventoryDate(String hotelId, String roomTypeId, LocalDate date);

    List<RoomInventory> findAllByInventoryDateBetween(LocalDate start, LocalDate end);


    @Query("SELECT i.hotelId AS hotelId, i.roomType.id AS roomTypeId " +
            "FROM RoomInventory i " +
            "WHERE i.roomType.id IN :roomTypeIds AND i.inventoryDate = :date")
    List<InventoryKeyProjection> findExistingInventoryKeys(
            @Param("roomTypeIds") List<String> roomTypeIds,
            @Param("date") LocalDate date);


}