package com.example.BookingService.scheduler;

import com.example.BookingService.Client.HotelClient;
import com.example.BookingService.Dto.RoomTypeExportDto;
import com.example.BookingService.entity.RoomInventory;
import com.example.BookingService.entity.RoomType;
import com.example.BookingService.projection.InventoryKeyProjection;
import com.example.BookingService.repository.InventoryRepo;
import com.example.BookingService.repository.RoomTypeRepo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventoryScheduler {

    private final RoomTypeRepo roomTypeRepo;
    private final InventoryRepo inventoryRepo;
    private final HotelClient hotelClient;
    private final Logger log = LoggerFactory.getLogger(InventoryScheduler.class);

    @Scheduled(cron = "0 0 1 * * *")
    public void scheduledSync() {
        syncAllInventory();
    }

    @Transactional
    @CircuitBreaker(name = "hotelServiceBreaker", fallbackMethod = "fallbackSync")
    @Retry(name = "hotelServiceRetry")
    public void syncAllInventory() {
        log.info("Attempting Remote Metadata Sync...");
        List<RoomTypeExportDto> metadata = hotelClient.fetchAllRoomMetadata();
        processSync(metadata, true);
    }

    @Transactional
    public void fallbackSync(Exception e) {
        log.warn("Hotel Service DOWN. Falling back to local DB.", e);

        List<RoomType> localRooms = roomTypeRepo.findAll();

        List<RoomTypeExportDto> fallbackMetadata = localRooms.stream()
                .map(r -> new RoomTypeExportDto(
                        r.getId(),
                        r.getHotelId(),
                        r.getName(),
                        r.getBasePrice(),
                        r.getDefaultCapacity()
                ))
                .toList();

        processSync(fallbackMetadata, false);
    }

    private void processSync(List<RoomTypeExportDto> hotelMetadata, boolean isRemote) {

        if (hotelMetadata.isEmpty()) {
            log.info("No metadata found. Skipping sync.");
            return;
        }

        LocalDate targetDate = LocalDate.now().plusDays(365);

        // 1. Collect IDs
        List<String> allRoomTypeIds = hotelMetadata.stream()
                .map(RoomTypeExportDto::getRoomTypeId)
                .distinct()
                .toList();

        // 2. Fetch truly existing IDs from DB
        Set<String> existingRoomTypeIdsInDb = new HashSet<>(
                roomTypeRepo.findExistingRoomTypeIds(allRoomTypeIds)
        );

        // 3. Fetch existing Inventory keys to avoid duplicates
        List<InventoryKeyProjection> existingInventory =
                inventoryRepo.findExistingInventoryKeys(allRoomTypeIds, targetDate);

        Set<Pair<String, String>> existingInventoryKeys = existingInventory.stream()
                .map(e -> Pair.of(e.getHotelId(), e.getRoomTypeId()))
                .collect(Collectors.toSet());

        List<RoomType> roomsToSave = new ArrayList<>();
        List<RoomInventory> inventoryToSave = new ArrayList<>();
        Map<String, RoomType> roomTypeMap = new HashMap<>();

        for (RoomTypeExportDto meta : hotelMetadata) {
            String roomTypeId = meta.getRoomTypeId();
            String hotelId = meta.getHotelId();

            // Check if we need to create the RoomType locally
            boolean existsInDb = existingRoomTypeIdsInDb.contains(roomTypeId);

            if (isRemote && !existsInDb && !roomTypeMap.containsKey(roomTypeId)) {
                RoomType room = RoomType.builder()
                        .id(roomTypeId)
                        .hotelId(hotelId)
                        .name(meta.getRoomName())
                        .basePrice(meta.getBasePrice())
                        .defaultCapacity(meta.getDefaultCapacity())
                        .build();

                roomsToSave.add(room);
                roomTypeMap.put(roomTypeId, room);
            }

            // Inventory logic
            Pair<String, String> key = Pair.of(hotelId, roomTypeId);

            // CRITICAL FIX: Only create inventory if the room exists in DB OR is being saved now
            if (!existingInventoryKeys.contains(key) && (existsInDb || roomTypeMap.containsKey(roomTypeId))) {

                // Get the object: either the one we just built, or a safe proxy for one that exists
                RoomType room = roomTypeMap.containsKey(roomTypeId)
                        ? roomTypeMap.get(roomTypeId)
                        : roomTypeRepo.getReferenceById(roomTypeId);

                RoomInventory inventory = RoomInventory.builder()
                        .hotelId(hotelId)
                        .roomType(room)
                        .inventoryDate(targetDate)
                        .totalCapacity(meta.getDefaultCapacity())
                        .bookedCount(0)
                        .build();

                inventoryToSave.add(inventory);
                existingInventoryKeys.add(key);
            } else if (!existsInDb && !roomTypeMap.containsKey(roomTypeId)) {
                log.warn("Skipping inventory for Room ID {} because it does not exist in local DB and couldn't be synced.", roomTypeId);
            }
        }

        // 4. Batch save (RoomTypes MUST be flushed first so Inventory can point to them)
        if (!roomsToSave.isEmpty()) {
            roomTypeRepo.saveAll(roomsToSave);
            roomTypeRepo.flush();
            log.info("Saved {} RoomTypes", roomsToSave.size());
        }

        if (!inventoryToSave.isEmpty()) {
            inventoryRepo.saveAll(inventoryToSave);
            log.info("Saved {} Inventory records (Source: {})",
                    inventoryToSave.size(),
                    isRemote ? "REMOTE" : "FALLBACK");
        }

        log.info("Inventory sync completed.");
    }
}