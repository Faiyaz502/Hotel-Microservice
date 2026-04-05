package com.hotel.service.HotelService.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
@Document(indexName = "hotels")
public class HotelIndex {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard", copyTo = "name_suggest")
    private String name;

    // Sub-field for Autocomplete
    @Field(name = "name_suggest", type = FieldType.Text, analyzer = "autocomplete_analyzer")
    private String nameSuggest;

    @Field(type = FieldType.Keyword) // Keyword is required for Aggregations/Sorting
    private String location;

    @Field(type = FieldType.Double)
    private Double avgRating;
}