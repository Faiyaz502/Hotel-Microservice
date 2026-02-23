package com.example.NotifationService.Consumer;

import com.example.NotifationService.payload.UserCreatedEvent;

import com.example.NotifationService.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {



    private final EmailService emailService;

    @KafkaListener(
            topics = "user-created",
            groupId = "user-service-consumer-group"
    )
    public void consumeUserCreated(UserCreatedEvent event) {

        log.info("Received event for userId: {}", event.getUserId());

        processUserCreatedEvent(event);
    }

    private void processUserCreatedEvent(UserCreatedEvent event) {


        log.info(" ----Processing user created event for userId: {}", event.getUserId());

        log.info(" ----Processing user created event for userId: {}", event.getEmail());



        //  SEND EMAIL
        emailService.sendWelcomeEmail(
                event.getEmail(),
                event.getUsername()
        );

        log.info("---- yoro ujsq lwpl kumw Successfully processed user created event for userId: {}", event.getUserId());
    }
}
