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

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;


    @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "standard")
    private String nameSuggest;

    @Field(type = FieldType.Keyword)
    private String location;

    @Field(type = FieldType.Double)
    private Double avgRating;
}