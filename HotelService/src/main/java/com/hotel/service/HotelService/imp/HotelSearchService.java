package com.hotel.service.HotelService.imp;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.hotel.service.HotelService.Dto.HotelResponse;
import com.hotel.service.HotelService.Dto.PaginatedResponse;
import com.hotel.service.HotelService.entities.HotelIndex;
import com.hotel.service.HotelService.multipleDbManagement.ReadOnly;
import com.hotel.service.HotelService.services.HotelService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelSearchService {

    private final ElasticsearchClient client;
    private final HotelService hotelService;
    private final Logger log = LoggerFactory.getLogger(HotelSearchService.class);

    @ReadOnly
    @Cacheable(
            value = "hotel_search",
            key = "{#name, #location, #minRating, #lastId, #lastScore}",
            unless = "#result.getContent().isEmpty()"
    )
    @CircuitBreaker(name = "hotelSearchCB", fallbackMethod = "jpaFallback")
    public PaginatedResponse<HotelResponse> searchAdvanced(
            String name,
            String location,
            Double minRating,
            String lastId,
            Double lastScore,
            int size) throws IOException {

        log.info("Advanced Search (ES Client): name={}, location={}, minRating={}", name, location, minRating);

        try {
            // Use query_string style search like curl
            SearchResponse<HotelIndex> response = client.search(s -> s
                            .index("hotels")
                            .query(q -> {
                                if (name != null && !name.isBlank()) {
                                    return q.queryString(qs -> qs
                                            .query(name)
                                            .fields("name^2", "nameSuggest")
                                    );
                                }
                                // Fallback to match_all if no name provided
                                return q.matchAll(m -> m);
                            })
                            .size(size)
                            .sort(so -> so.field(f -> f.field("avgRating").order(SortOrder.Desc).missing("_last"))),

                    HotelIndex.class
            );

            // Map results
            List<HotelResponse> content = response.hits().hits().stream()
                    .map(Hit::source)
                    .map(h -> new HotelResponse(
                            h.getId(),
                            h.getName(),
                            h.getLocation(),
                            h.getAvgRating() != null ? h.getAvgRating() : 0.0,
                            0.0 // optional: score if needed
                    ))
                    .toList();

            // Cursor pagination (optional)
            String nextId = null;
            Double nextScore = null;
            if (!content.isEmpty() && content.size() >= size) {
                HotelResponse lastItem = content.get(content.size() - 1);
                nextId = lastItem.id();
                nextScore = response.hits().hits().get(content.size() - 1).score();
            }

            return new PaginatedResponse<>(content, nextId, nextScore);

        } catch (Exception e) {
            log.error("❌ Elasticsearch failed: ", e);
            throw e; // Circuit breaker will call fallback
        }
    }

    @ReadOnly
    public PaginatedResponse<HotelResponse> jpaFallback(
            String name, String location, Double minRating,
            String lastId, Double lastScore, int size, Throwable t) {

        log.error("⚠️ ES DOWN → Falling back to DB: {}", t.getMessage());
        return hotelService.getHotelsPaginated(name, location, lastId, size);
    }
}