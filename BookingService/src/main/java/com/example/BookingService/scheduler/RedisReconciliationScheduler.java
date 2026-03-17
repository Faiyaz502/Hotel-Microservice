package com.example.BookingService.scheduler;

import com.example.BookingService.entity.RoomInventory;
import com.example.BookingService.repository.InventoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisReconciliationScheduler {

    private final InventoryRepo inventoryRepo;
    private final RedisTemplate<String, String> redisTemplate;

    // Runs every 30 minutes to ensure Redis isn't "drifting" too far from DB
    @Scheduled(cron = "0 0/30 * * * *")
    public void syncRedisWithDB() {
        log.info("Starting Redis-DB reconciliation...");

        // In a real system, you'd only do this for "hot" dates (next 30 days)
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);
        List<LocalDate> dates = start.datesUntil(end).toList();

        // 1. Fetch all inventories for this window
        // (Pseudocode: You'd likely batch this by Hotel/RoomType)
        List<RoomInventory> activeInventories = inventoryRepo.findAllByInventoryDateBetween(start, end);

        for (RoomInventory inv : activeInventories) {
            String key = "hold:{" + inv.getHotelId() + ":" + inv.getRoomTypeId() + "}:" + inv.getInventoryDate();

            // If there are no active holds in Redis, we don't need to do anything.
            // But if there are, we can verify them against a 'live_holds' count if we tracked it.
            // Simplest fix: If Redis value > 0 and it's been a while, we can reset or audit.
        }
    }
}
