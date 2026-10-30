package com.pm.bookingservice.integration.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class BookingEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(BookingEventProducer.class);

    private static final long ACK_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public BookingEventProducer(
            KafkaTemplate<String, byte[]> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(
            String topic,
            String key,
            byte[] payload
    ) throws ExecutionException, InterruptedException, TimeoutException {

        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException(
                    "Kafka topic must not be blank."
            );
        }

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Kafka message key must not be blank."
            );
        }

        if (payload == null || payload.length == 0) {
            throw new IllegalArgumentException(
                    "Kafka payload must not be empty."
            );
        }

        try {
            kafkaTemplate
                    .send(topic, key, payload)
                    .get(ACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.info(
                    "Kafka acknowledged event. topic={}, key={}",
                    topic,
                    key
            );

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw ex;

        } catch (ExecutionException | TimeoutException ex) {
            log.error(
                    "Kafka publication was not confirmed. topic={}, key={}",
                    topic,
                    key,
                    ex
            );

            throw ex;
        }
    }
}
