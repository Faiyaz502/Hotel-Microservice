package com.hotel.service.HotelService.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default configuration: 10 minutes
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        // Specific configurations for our caches
        Map<String, RedisCacheConfiguration> configurations = new HashMap<>();

        // 1. Search Results: 5 mins (Short - prevents stale data in lists)
        configurations.put("hotel_search", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 2. Single Hotel: 60 mins (Long - these details don't change often)
        configurations.put("hotel", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 3. Global List: 10 mins
        configurations.put("hotels", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configurations)
                .build();
    }
}
