package com.hotel.service.HotelService.schedulers;

import com.hotel.service.HotelService.Repositories.HotelRepo;
import com.hotel.service.HotelService.Repositories.HotelSearchRepository;
import com.hotel.service.HotelService.entities.Hotel;
import com.hotel.service.HotelService.entities.HotelIndex;
import com.hotel.service.HotelService.multipleDbManagement.ReadOnly;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelSyncScheduler {

    private final HotelRepo hotelRepo;
    private final HotelSearchRepository searchRepo;
    private final Logger log = LoggerFactory.getLogger(HotelSyncScheduler.class);

   //Every 5min
    @Scheduled(fixedRate = 300000)
    @ReadOnly
    public void syncHotelsToElasticsearch() {
        log.info("Starting Background Sync: SQL Replicas -> Elasticsearch");

        //Look for hotels updated in the last 6 minutes (to ensure no gap)
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(6);
        List<Hotel> modifiedHotels = hotelRepo.findByUpdatedAtAfter(threshold);

        if (modifiedHotels.isEmpty()) {
            log.info("Sync Complete: No changes detected in the last 5 minutes.");
            return;
        }

        // Map JPA Entities to Elasticsearch Documents
        List<HotelIndex> indexData = modifiedHotels.stream()
                .map(hotel -> HotelIndex.builder()
                        .id(hotel.getId())
                        .name(hotel.getName())
                        .location(hotel.getLocation())
                        .avgRating(hotel.getAvgRating())
                        .build())
                .collect(Collectors.toList());

        //  Bulk Save (Much more efficient than saving one by one)
        try {
            searchRepo.saveAll(indexData);
            log.info("Sync Success: Pushed {} updated hotels to Elasticsearch index.", indexData.size());
        } catch (Exception e) {
            log.error("Sync Failed: Could not connect to Elasticsearch. Error: {}", e.getMessage());
        }
    }
}