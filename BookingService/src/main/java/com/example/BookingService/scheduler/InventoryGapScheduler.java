package com.example.BookingService.scheduler;



import com.example.BookingService.Client.HotelClient;
import com.example.BookingService.Dto.RoomTypeExportDto;
import com.example.BookingService.entity.RoomInventory;
import com.example.BookingService.entity.RoomType;
import com.example.BookingService.repository.InventoryRepo;
import com.example.BookingService.repository.RoomTypeRepo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryGapScheduler {

    private final RoomTypeRepo roomTypeRepo;
    private final InventoryRepo inventoryRepo;
    private final HotelClient hotelClient;

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledGapFill() {
        fillNextTwoMonthsInventory();
    }

    @Transactional
    @CircuitBreaker(name = "hotelServiceBreaker", fallbackMethod = "fallbackGapFill")
    @Retry(name = "hotelServiceRetry")
    public void fillNextTwoMonthsInventory() {
        log.info("Gap-Filler: Attempting Remote Sync...");
        List<RoomTypeExportDto> metadata = hotelClient.fetchAllRoomMetadata();
        processGapFill(metadata);
    }

    @Transactional
    public void fallbackGapFill(Exception e) {
        log.warn("Gap-Filler: Hotel Service DOWN. Patching gaps from Local DB blueprints.");

        // Convert Local RoomTypes into DTOs to reuse the same processing logic
        List<RoomTypeExportDto> fallbackMetadata = roomTypeRepo.findAll().stream()
                .map(r -> new RoomTypeExportDto(
                        r.getId(),
                        r.getHotelId(),
                        r.getName(),
                        r.getBasePrice(),
                        r.getDefaultCapacity()
                ))
                .toList();

        processGapFill(fallbackMetadata);
    }

    private void processGapFill(List<RoomTypeExportDto> metadata) {
        if (metadata.isEmpty()) {
            log.info("No metadata found for gap filling.");
            return;
        }

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(2);

        //  Fetch all existing record keys in ONE query (Bulk Key Strategy)
        Set<String> existingKeys = inventoryRepo.findAllExistingKeys(start, end);

        List<RoomInventory> buffer = new ArrayList<>();
        int BATCH_SIZE = 500;

        for (RoomTypeExportDto meta : metadata) {
            // Get a reference to the RoomType entity (Safe Proxy)
            RoomType roomRef = roomTypeRepo.getReferenceById(meta.getRoomTypeId());

            for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {

                String currentKey = meta.getHotelId() + "_" + meta.getRoomTypeId() + "_" + date;

                // 2. If the slot is missing, fill the gap
                if (!existingKeys.contains(currentKey)) {
                    buffer.add(RoomInventory.builder()
                            .hotelId(meta.getHotelId())
                            .roomType(roomRef)
                            .inventoryDate(date)
                            .totalCapacity(meta.getDefaultCapacity())
                            .bookedCount(0)
                            .build());
                }

                if (buffer.size() >= BATCH_SIZE) {
                    inventoryRepo.saveAll(buffer);
                    buffer.clear();
                }
            }
        }

        if (!buffer.isEmpty()) {
            inventoryRepo.saveAll(buffer);
        }
        log.info("Inventory gap-filler completed.");
    }
}