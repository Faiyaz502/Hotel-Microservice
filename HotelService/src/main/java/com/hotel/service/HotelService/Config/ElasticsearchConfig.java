package com.hotel.service.HotelService.Config;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {

        RestClientBuilder builder = RestClient.builder(
                new org.apache.http.HttpHost("localhost", 9200, "http")
        );

        RestClient restClient = builder.build();

        RestClientTransport transport =
                new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
