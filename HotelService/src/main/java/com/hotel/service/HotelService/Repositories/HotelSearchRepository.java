package com.hotel.service.HotelService.Repositories;

import com.hotel.service.HotelService.entities.HotelIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelSearchRepository extends ElasticsearchRepository<HotelIndex, String> {



}