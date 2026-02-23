package com.user.service.UserService.Impl;

import com.user.service.UserService.Config.AppConstants;
import com.user.service.UserService.Payload.UserCreatedEvent;
import com.user.service.UserService.entities.User;
import com.user.service.UserService.service.KafkaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaImp implements KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Logger logger = LoggerFactory.getLogger(KafkaImp.class);

    @Override
    public Boolean userCreatedPublisher(User user) {
        logger.info("Calling Kafka UserCreate Publisher for userId: {}", user.getUserId());

        // Build a JSON string with user info
        String message = String.format(
                "{\"userId\":\"%s\", \"email\":\"%s\", \"username\":\"%s\", \"action\":\"created\"}",
                user.getUserId(),
                user.getEmail(),
                user.getName()
        );

        logger.info("Sending message to Kafka topic 'user-created': {}", message);

        // Send with userId as key
        kafkaTemplate.send("user-created", user.getUserId(), message);

        logger.info("Message sent successfully to Kafka");

        return true;
    }
}
