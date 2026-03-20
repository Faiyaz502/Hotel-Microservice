package com.example.BookingService.scheduler;

import com.example.BookingService.Client.HotelClient;
import com.example.BookingService.Dto.RoomTypeExportDto;
import com.example.BookingService.entity.RoomInventory;
import com.example.BookingService.entity.RoomType;
import com.example.BookingService.repository.InventoryRepo;
import com.example.BookingService.repository.RoomTypeRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryScheduler {

    private final RoomTypeRepo roomTypeRepo;
    private final InventoryRepo inventoryRepo;
    private final HotelClient hotelClient;

    /**
     * Daily roll-forward inventory for exactly 1 year ahead.
     * Runs at 2:00 AM server time.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void rollForwardInventory() {
        log.info("Starting daily inventory roll-forward...");

        LocalDate targetDate = LocalDate.now().plusDays(365);
        List<RoomType> allRoomTypes = roomTypeRepo.findAll();
        List<RoomInventory> newInventoryRows = new ArrayList<>();

        for (RoomType type : allRoomTypes) {
            boolean exists = inventoryRepo.existsByHotelAndRoomTypeAndDate(
                    type.getHotelId(), type.getId(), targetDate);

            if (!exists) {
                RoomInventory inv = new RoomInventory();
                inv.setHotelId(type.getHotelId());
                inv.setRoomTypeId(type.getId());
                inv.setInventoryDate(targetDate);
                inv.setTotalCapacity(type.getDefaultCapacity());
                inv.setBookedCount(0);
                newInventoryRows.add(inv);
            }
        }

        if (!newInventoryRows.isEmpty()) {
            inventoryRepo.saveAll(newInventoryRows);
            log.info("Added {} inventory rows for {}", newInventoryRows.size(), targetDate);
        } else {
            log.info("No new inventory rows needed for {}", targetDate);
        }
    }





    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void syncAllInventory() {
        log.info("Fetching all room metadata from Hotel Service...");

        // 1. Get ALL data in ONE request
        List<RoomTypeExportDto> hotelMetadata = hotelClient.fetchAllRoomMetadata();

        LocalDate targetDate = LocalDate.now().plusDays(365);
        List<RoomInventory> newRows = new ArrayList<>();

        for (RoomTypeExportDto meta : hotelMetadata) {
            // 2. Check if we already have inventory for this room on this date
            boolean exists = inventoryRepo.existsByHotelAndRoomTypeAndDate(
                    meta.getHotelId(), meta.getRoomTypeId(), targetDate);

            if (!exists) {
                newRows.add(RoomInventory.builder()
                        .hotelId(meta.getHotelId())
                        .roomTypeId(meta.getRoomTypeId())
                        .inventoryDate(targetDate)
                        .totalCapacity(meta.getDefaultCapacity())
                        .bookedCount(0)
                        .build());
            }
        }

        if (!newRows.isEmpty()) {
            inventoryRepo.saveAll(newRows);
            log.info("Batch Sync Complete: Added {} new inventory rows.", newRows.size());
        }
    }




}
