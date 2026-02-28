package com.hotel.service.HotelService.imp;

import com.hotel.service.HotelService.Dto.HotelProjection;
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
    @Cacheable(value = "hotel_search",
            key = "{#name, #location, #lastId, #size}",
            unless = "#result.content.isEmpty()")
    public PaginatedResponse<HotelProjection> getHotelsPaginated(String name, String location, String lastId, int size) {

        Specification<Hotel> spec = HotelSpecs.getSpec(name, location, lastId);

        //   (size + 1) for next page check
        Pageable pageable = PageRequest.of(0, size + 1, Sort.by("id").ascending());

        // Fetch the list -> size + 1
        List<HotelProjection> projections = hotelRepo.findAllProjectedBy(spec, pageable);

        String nextCursor = null;
        List<HotelProjection> resultList;

        // Logic to determine if there is a next page
        if (projections.size() > size) {
            // We found the "peek" record! There is a next page.
            nextCursor = projections.get(size - 1).getId();
            // Remove the extra (size + 1) record so the user only gets exactly what they asked for
            resultList = projections.subList(0, size);
        } else {
            // No extra record found, this is the last page
            resultList = projections;
        }

        return new PaginatedResponse<>(resultList, nextCursor);
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
