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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Service
@RequiredArgsConstructor
public class HotelServiceImp implements HotelService {

    private final HotelRepo hotelRepo;
    private final Logger log = LoggerFactory.getLogger(HotelServiceImp.class);

    // Create-----
    @Override
    @Caching(evict = {
            @CacheEvict(value = "hotel_search", allEntries = true), // ---Clears all search results
            @CacheEvict(value = "hotels", allEntries = true)        //--- Clears your "getAll" list
    })
    public Hotel create(Hotel hotel) {

        return hotelRepo.save(hotel);
    }

    // get All Hotels ---Testing perpose -----
    @Override
    @Cacheable(value = "hotels")
    public List<Hotel> getAllHotels() {

        log.info("------Getting All Hotels from DB--------");
        return hotelRepo.findAll();
    }


    @Override
    @Cacheable(
            value = "hotel_search",
            key = "{#name, #location, #lastId, #size}",
            unless = "#result.content.isEmpty()"
    )
    public PaginatedResponse<HotelResponse> getHotelsPaginated(
            String name,
            String location,
            String lastId,
            int size
    ) {
        //--- Ensure size is reasonable to prevent Memory/DDoS issues
        int fetchSize = Math.min(size, 100);

        Specification<Hotel> spec = HotelSpecs.getSpec(name, location, lastId);

        //---Fetch using Fluent API - Efficient Index Seek
        List<HotelProjection> projections = hotelRepo.findBy(spec, q -> q
                .as(HotelProjection.class)
                .sortBy(Sort.by("id").ascending())
                .limit(fetchSize + 1) // Fetch 1 extra to check for the next page
                .all()
        );

        // --Handle Empty Results early
        if (projections.isEmpty()) {
            return new PaginatedResponse<>(Collections.emptyList(), null);
        }

        // ---Map to Record - Use Streams for readability and immutability
        List<HotelResponse> content = projections.stream()
                .limit(fetchSize) // Don't include the extra "check" record in the result
                .map(p -> new HotelResponse(
                        p.getId(),
                        p.getName(),
                        p.getLocation(),
                        p.getAvgRating()
                ))
                .toList();

        //== Determine the Next Cursor
        String nextCursor = null;
        if (projections.size() > fetchSize) {
            // The last item (index fetchSize) is the starting point for the next request
            nextCursor = projections.get(fetchSize).getId();
        }


        log.info("Hotel Search: name={}, loc={}, results={}, hasNext={}",
                name, location, content.size(), nextCursor != null);

        return new PaginatedResponse<>(content, nextCursor);
    }


    @Override
    @Cacheable(value = "hotel",key = "#hotelId")
    public HotelSummaryDto getHotelById(String hotelId) {

        log.info("------Getting  Hotel from DB- ID : ->{}",hotelId);

            Hotel Hotel = hotelRepo.findById(hotelId).orElseThrow(()-> new ResourceNotFoundException("Hotel Not Found"));
            return toHotelDto(Hotel);


    }


    // -----UPDATE--
    @Override
    @Caching(evict = {
            @CacheEvict(value = "hotels", allEntries = true),
            @CacheEvict(value = "hotel_search", allEntries = true)
    })
    @CachePut(value = "hotel", key = "#id")
    public HotelSummaryDto updateHotel(String id, Hotel hotelDetails) {
        log.info("Updating Hotel ID: {}", id);

        //------ Get the existing full record
        Hotel existingHotel = hotelRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Not Found"));

        //----- Update only the allowed fields
        if (hotelDetails.getName() != null) existingHotel.setName(hotelDetails.getName());
        if (hotelDetails.getLocation() != null) existingHotel.setLocation(hotelDetails.getLocation());
        if (hotelDetails.getAbout() != null) existingHotel.setAbout(hotelDetails.getAbout());
        if (hotelDetails.getContact() != null) existingHotel.setContact(hotelDetails.getContact());

        Hotel Hotel = hotelRepo.save(existingHotel);


        return toHotelDto(Hotel);
    }

    // -----DELETE
    @Override
    @Caching(evict = {
            @CacheEvict(value = "hotel", key = "#hotelId"),
            @CacheEvict(value = "hotels", allEntries = true),
            @CacheEvict(value = "hotel_search", allEntries = true)
    })
    public void deleteHotel(String hotelId) {
        int deletedCount = hotelRepo.deleteByIdIfNotNull(hotelId);
        if (deletedCount == 0) {
            // No hotel deleted, either null or invalid ID
            throw new ResourceNotFoundException("Hotel not found or invalid ID");
        }
    }



    //Converting Dto-----------

    private HotelSummaryDto toHotelDto(Hotel Hotel){


        return HotelSummaryDto.builder()
                .id(Hotel.getId())
                .name(Hotel.getName())
                .avgRating(Hotel.getAvgRating())
                .location(Hotel.getLocation())
                .about(Hotel.getAbout())
                .contact(Hotel.getContact()).build();


    }




}
