package com.example.BookingService.kafkaService;

import com.example.BookingService.Dto.BookingEvent;
import com.example.BookingService.entity.Booking;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);


    public Boolean bookingCreatedPublisher(Booking booking) {

        logger.info("Publishing booking event for bookingId: {}", booking.getId());

        String message = String.format(
                "{\"bookingId\":\"%s\", \"userId\":\"%s\", \"hotelId\":\"%s\", \"status\":\"CREATED\"}",
                booking.getId(),
                booking.getUserId(),
                booking.getHotelId()
        );

        logger.info("Sending message to Kafka topic 'booking-events': {}", message);

        kafkaTemplate.send("booking-events", booking.getId(), message);

        logger.info("Booking event sent successfully");

        return true;
    }
}
