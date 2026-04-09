package com.hotel.service.HotelService.schedulers;

import com.hotel.service.HotelService.Repositories.HotelRepo;
import com.hotel.service.HotelService.entities.Hotel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FullDatabaseSyncService {

    private final HotelRepo primaryRepo; // Connected to Primary DB
    private final Map<String, DataSource> replicaDataSources; // Map of replicaName -> DataSource
    private final Logger log = LoggerFactory.getLogger(FullDatabaseSyncService.class);

    /**
     * Scheduled to run every 5 minutes and sync updated records
     * from primary DB to all replicas.
     */
    @Scheduled(fixedRate = 300000)
    @Transactional(readOnly = true)
    public void syncAllDataToReplicas() {
        log.info("Starting Replica DB Sync from Primary...");

        // 1. Fetch all hotels from primary DB
        List<Hotel> hotels = primaryRepo.findAll();

        if (hotels.isEmpty()) {
            log.warn("Primary DB is empty. Nothing to sync.");
            return;
        }

        log.info("Fetched {} hotels from Primary DB.", hotels.size());

        // 2. Sync each replica
        replicaDataSources.forEach((replicaName, ds) -> {
            if (replicaName.toLowerCase().contains("replica")) {
                try {
                    pushToReplica(replicaName, ds, hotels);
                } catch (Exception e) {
                    log.error("Failed to sync to {}: {}", replicaName, e.getMessage());
                }
            }
        });

        log.info("Replica DB Sync completed.");
    }

    private void pushToReplica(String replicaName, DataSource ds, List<Hotel> hotels) {
        JdbcTemplate jdbc = new JdbcTemplate(ds);

        // Ensure table exists on replica
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS hotels (
                id VARCHAR(255) PRIMARY KEY,
                name VARCHAR(255),
                location VARCHAR(255),
                avg_rating DOUBLE,
                updated_at TIMESTAMP
            )
        """);

        // Batch update query
        String sql = """
            INSERT INTO hotels (id, name, location, avg_rating, updated_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name,
                location = EXCLUDED.location,
                avg_rating = EXCLUDED.avg_rating,
                updated_at = EXCLUDED.updated_at
        """;

        jdbc.batchUpdate(sql, hotels, 100, (ps, hotel) -> {
            ps.setString(1, hotel.getId());
            ps.setString(2, hotel.getName());
            ps.setString(3, hotel.getLocation());
            // Safely handle null avgRating
            ps.setObject(4, hotel.getAvgRating() != null ? hotel.getAvgRating() : 0.0);
            ps.setObject(5, hotel.getUpdatedAt());
        });

        log.info("Successfully synced {} records to {}", hotels.size(), replicaName);
    }
}