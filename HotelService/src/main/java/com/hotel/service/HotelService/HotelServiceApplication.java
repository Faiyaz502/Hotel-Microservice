package com.hotel.service.HotelService;

import com.hotel.service.HotelService.Repositories.HotelSearchRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories(
        basePackages = "com.hotel.service.HotelService.Repositories",
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {com.hotel.service.HotelService.Repositories.HotelRepo.class,
                        com.hotel.service.HotelService.Repositories.RoomTypeRepo.class,
                        com.hotel.service.HotelService.Repositories.StaffRepo.class})
)
@EnableElasticsearchRepositories(
        basePackages = "com.hotel.service.HotelService.Repositories",
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = com.hotel.service.HotelService.Repositories.HotelSearchRepository.class)
)
public class HotelServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelServiceApplication.class, args);
	}

}
