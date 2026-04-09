package com.hotel.service.HotelService.imp;

import com.hotel.service.HotelService.Dto.HotelProjection;
import com.hotel.service.HotelService.Dto.HotelResponse;
import com.hotel.service.HotelService.Dto.HotelSummaryDto;
import com.hotel.service.HotelService.Dto.PaginatedResponse;
import com.hotel.service.HotelService.Exceptions.ResourceNotFoundException;
import com.hotel.service.HotelService.Filter.HotelSpecs;
import com.hotel.service.HotelService.Repositories.HotelRepo;
import com.hotel.service.HotelService.entities.Hotel;
import com.hotel.service.HotelService.services.HotelService;
import com.hotel.service.HotelService.multipleDbManagement.ReadOnly; // <--- Custom AOP Annotation
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // <--- For sync tracking
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelServiceImp implements HotelService {

    private final HotelRepo hotelRepo;
    private final Logger log = LoggerFactory.getLogger(HotelServiceImp.class);

    @Override
    @Transactional // Hits PRIMARY DB
    @Caching(evict = {
            @CacheEvict(value = "hotel_search", allEntries = true),
            @CacheEvict(value = "hotels", allEntries = true)
    })
    public Hotel create(Hotel hotel) {
        // Set timestamp so the Scheduler can find this for Elasticsearch sync
        hotel.setUpdatedAt(LocalDateTime.now());
        return hotelRepo.save(hotel);
    }

    @Override
    @ReadOnly // --- Routes this query to Replicas 1, 2, or 3
    @Cacheable(
            value = "hotel_search",
            key = "{#name, #location, #lastId, #size}",
            unless = "#result.getContent().isEmpty()"
    )
    public PaginatedResponse<HotelResponse> getHotelsPaginated(
            String name,
            String location,
            String lastId,
            int size
    ) {
        int fetchSize = Math.min(size, 100);
        Specification<Hotel> spec = HotelSpecs.getSpec(name, location, lastId);

        List<HotelProjection> projections = hotelRepo.findBy(spec, q -> q
                .as(HotelProjection.class)
                .sortBy(Sort.by("id").ascending())
                .limit(fetchSize + 1)
                .all()
        );

        if (projections.isEmpty()) {
            return new PaginatedResponse<>(Collections.emptyList(), null, null);
        }

        List<HotelResponse> content = projections.stream()
                .limit(fetchSize)
                .map(p -> new HotelResponse(
                        p.getId(),
                        p.getName(),
                        p.getLocation(),
                        p.getAvgRating(),
                        null // DB doesn't have ES Score
                ))
                .toList();

        String nextCursor = null;
        if (projections.size() > fetchSize) {
            nextCursor = projections.get(fetchSize).getId();
        }

        return new PaginatedResponse<>(content, nextCursor, null);
    }

    @Override
    @ReadOnly // <--- Hits Replicas
    @Cacheable(value = "hotel", key = "#hotelId")
    public HotelSummaryDto getHotelById(String hotelId) {
        log.info("Getting Hotel from Replica DB - ID: {}", hotelId);
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Not Found"));
        return toHotelDto(hotel);
    }

    @Override
    @Transactional // Hits PRIMARY DB
    @Caching(evict = {
            @CacheEvict(value = "hotels", allEntries = true),
            @CacheEvict(value = "hotel_search", allEntries = true)
    })
    @CachePut(value = "hotel", key = "#id")
    public HotelSummaryDto updateHotel(String id, Hotel hotelDetails) {
        Hotel existingHotel = hotelRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Not Found"));

        if (hotelDetails.getName() != null) existingHotel.setName(hotelDetails.getName());
        if (hotelDetails.getLocation() != null) existingHotel.setLocation(hotelDetails.getLocation());
        if (hotelDetails.getAbout() != null) existingHotel.setAbout(hotelDetails.getAbout());
        if (hotelDetails.getContact() != null) existingHotel.setContact(hotelDetails.getContact());

        // Update timestamp for Scheduler sync
        existingHotel.setUpdatedAt(LocalDateTime.now());

        Hotel saved = hotelRepo.save(existingHotel);
        return toHotelDto(saved);
    }

    @Override
    @Transactional // Hits PRIMARY DB
    @Caching(evict = {
            @CacheEvict(value = "hotel", key = "#hotelId"),
            @CacheEvict(value = "hotels", allEntries = true),
            @CacheEvict(value = "hotel_search", allEntries = true)
    })
    public void deleteHotel(String hotelId) {
        int deletedCount = hotelRepo.deleteByIdIfNotNull(hotelId);
        if (deletedCount == 0) {
            throw new ResourceNotFoundException("Hotel not found or invalid ID");
        }
    }

    private HotelSummaryDto toHotelDto(Hotel hotel) {
        return HotelSummaryDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .avgRating(Double.valueOf(hotel.getAvgRating()))
                .location(hotel.getLocation())
                .about(hotel.getAbout())
                .contact(hotel.getContact())
                .build();
    }
}