package com.user.service.UserService.Config;

import com.user.service.UserService.Interceptor.InternalKeyInterceptor;
import com.user.service.UserService.Payload.UserCreatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class MyConfig {



    @Bean
    @LoadBalanced  // to use the service for calling
    public RestTemplate restTemplate(){

        RestTemplate restTemplate = new RestTemplate();

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new InternalKeyInterceptor());
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }

    //-------Kafka--------
    @Bean
    public NewTopic topic(){

        return TopicBuilder.name("user-created").build();
    }

    @Bean
    CommandLineRunner testKafka(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> {
            kafkaTemplate.send("user-created", "Hello Kafka!");
            System.out.println("Sent test message");
        };
    }






}
