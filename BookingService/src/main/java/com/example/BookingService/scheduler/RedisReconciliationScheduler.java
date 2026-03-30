package com.example.BookingService.scheduler;

import com.example.BookingService.entity.RoomInventory;
import com.example.BookingService.repository.InventoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisReconciliationScheduler {

    private final InventoryRepo inventoryRepo;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0/5 * * * *") // Run every 5 minutes during rush
    public void syncRedisWithDB() {
        log.info("Executing High-Precision Redis Sync...");

        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(7); // Focus on the next 7 days (the "Rush" window)

        List<RoomInventory> activeInventories = inventoryRepo.findAllByInventoryDateBetween(today, end);

        for (RoomInventory inv : activeInventories) {
            String key = "hold:{" + inv.getHotelId() + ":" + inv.getRoomType().getId() + "}:" + inv.getInventoryDate();

            try {
                Long ttl = redisTemplate.getExpire(key); // Get remaining TTL in seconds
                String redisValStr = redisTemplate.opsForValue().get(key);
                int redisVal = (redisValStr != null) ? Integer.parseInt(redisValStr) : 0;
                int dbBooked = inv.getBookedCount();

                // If TTL is > 290, a Lua script JUST incremented this.
                // We skip this specific key to avoid "stealing" a live user's hold.
                if (ttl != null && ttl > 290) {
                    log.info("Skipping active hold for key: {}", key);
                    continue;
                }

                if (redisVal != dbBooked) {
                    // If the key is 'leaked' (Redis > DB) or 'missing' (Redis < DB)
                    // and it's not a brand new hold, we force sync it.
                    redisTemplate.opsForValue().set(key, String.valueOf(dbBooked), Duration.ofHours(24));
                    log.warn("Rush Sync applied to {}: Redis {} -> DB {}", key, redisVal, dbBooked);
                }

            } catch (Exception e) {
                log.error("Error syncing key {}", key, e);
            }
        }
    }
}