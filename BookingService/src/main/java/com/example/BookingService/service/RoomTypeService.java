package com.example.BookingService.service;

import com.example.BookingService.entity.RoomInventory;
import com.example.BookingService.entity.RoomType;
import com.example.BookingService.repository.InventoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final InventoryRepo inventoryRepo;

    /**
     * Initialize inventory for a new room type for 1 year.
     */
    @Transactional
    public void bootstrapInventory(RoomType roomType) {
        List<RoomInventory> initialInventory = new ArrayList<>();
        LocalDate start = LocalDate.now();

        for (int i = 0; i < 365; i++) {
            RoomInventory inv = new RoomInventory();
            inv.setHotelId(roomType.getHotelId());
            inv.setRoomTypeId(roomType.getId());
            inv.setInventoryDate(start.plusDays(i));
            inv.setTotalCapacity(roomType.getDefaultCapacity());
            inv.setBookedCount(0);
            initialInventory.add(inv);
        }

        inventoryRepo.saveAll(initialInventory);
    }
}
