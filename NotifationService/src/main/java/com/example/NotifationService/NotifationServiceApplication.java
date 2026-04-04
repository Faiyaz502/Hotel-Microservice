package com.example.NotifationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class NotifationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotifationServiceApplication.class, args);
	}

}
