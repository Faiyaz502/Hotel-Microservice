package com.hotel.service.HotelService.imp;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.json.JsonData;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final HotelService hotelService;
    private final Logger log = LoggerFactory.getLogger(HotelSearchService.class);

    @ReadOnly
    @Cacheable(
            value = "hotel_search",
            key = "{#name, #location, #minRating, #lastId}",
            unless = "#result.content().isEmpty()"
    )
    @CircuitBreaker(name = "hotelSearchCB", fallbackMethod = "jpaFallback")
    public PaginatedResponse<HotelResponse> searchAdvanced(
            String name,
            String location,
            Double minRating,
            String lastId,
            Double lastScore,
            int size) {

        log.info("Advanced Search: name={}, location={}, minRating={}", name, location, minRating);

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    // Full-Text + Fuzzy Search
                    if (name != null && !name.isEmpty()) {
                        b.must(m -> m.match(mt -> mt
                                .field("nameSuggest")
                                .query(name)
                                .fuzziness("AUTO")
                                .prefixLength(2)
                        ));
                    }

                    // Filtering: location
                    if (location != null && !location.isEmpty()) {
                        b.filter(f -> f.term(t -> t.field("location").value(location)));
                    }

                    // Filtering: avgRating
                    if (minRating != null) {
                        b.filter(f -> f.range(r -> r
                                .number(n -> n.field("avgRating").gte(minRating))
                        ));
                    }

                    return b;
                }))
                // Sorting: avgRating desc, id asc for tie-breaker
                .withSort(Sort.by(Sort.Order.desc("avgRating"), Sort.Order.asc("id")))
                // Aggregation: average rating per location
                .withAggregation("avg_rating_per_location", Aggregation.of(a -> a
                        .terms(t -> t.field("location"))
                        .aggregations("avg_score", sa -> sa.avg(avg -> avg.field("avgRating")))
                ))
                // Suggesters: improved term suggestion
                .withSuggester(Suggester.of(s -> s
                        .suggesters("name-suggestion", sug -> sug
                                .text(name != null ? name : "")
                                .term(t -> t.field("name"))
                        )
                ))
                .withPageable(PageRequest.of(0, size));

        // Cursor pagination (searchAfter)
        if (lastId != null && lastScore != null) {
            queryBuilder.withSearchAfter(List.of(lastScore, lastId));
        }

        NativeQuery query = queryBuilder.build();

        SearchHits<HotelIndex> hits = elasticsearchOperations.search(query, HotelIndex.class);

        // Map to PaginatedResponse
        PaginatedResponse<HotelResponse> response = mapToPaginatedResponse(hits, size);


        return response;
    }

    @ReadOnly
    public PaginatedResponse<HotelResponse> jpaFallback(
            String name, String location, Double minRating,
            String lastId, Double lastScore, int size, Throwable t) {

        log.error("Elasticsearch Down: Falling back to DB Replicas. Error: {}", t.getMessage());
        // Standard DB fallback ignores score, uses lastId/size
        return hotelService.getHotelsPaginated(name, location, lastId, size);
    }

    private PaginatedResponse<HotelResponse> mapToPaginatedResponse(SearchHits<HotelIndex> hits, int size) {
        List<HotelResponse> content = hits.getSearchHits().stream()
                .map(hit -> {
                    // Extract the first sort value safely
                    double scoreValue = 0.0;
                    if (!hit.getSortValues().isEmpty()) {
                        Object val = hit.getSortValues().get(0);
                        if (val instanceof Number number) {
                            scoreValue = number.doubleValue();
                        }
                    }

                    return new HotelResponse(
                            hit.getContent().getId(),
                            hit.getContent().getName(),
                            hit.getContent().getLocation(),
                            hit.getContent().getAvgRating(),
                            scoreValue
                    );
                }).collect(Collectors.toList());

        String nextId = null;
        Double nextScore = null;

        if (!content.isEmpty() && content.size() >= size) {
            HotelResponse lastItem = content.get(content.size() - 1);
            nextId = lastItem.id();
            nextScore = lastItem.lastScore();
        }

        return new PaginatedResponse<>(content, nextId, nextScore);
    }
}