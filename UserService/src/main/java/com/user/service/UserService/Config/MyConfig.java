package com.user.service.UserService.Config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MyConfig {



    @Bean
    @LoadBalanced  // to use the service for calling
    public RestTemplate restTemplate(){

        return new RestTemplate();
    }


}
