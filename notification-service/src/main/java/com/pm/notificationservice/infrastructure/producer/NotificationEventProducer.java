package com.pm.notificationservice.infrastructure.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class NotificationEventProducer {

    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationEventProducer(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(
            String topic,
            String key,
            String message
    ) {
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(message, "message must not be null");

        if (topic.isBlank()) {
            throw new IllegalArgumentException("topic must not be blank");
        }

        if (key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }

        try {
            kafkaTemplate.send(topic, key, message)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while publishing notification event to Kafka.",
                    ex
            );

        } catch (ExecutionException | TimeoutException ex) {
            throw new IllegalStateException(
                    "Failed to publish notification event to Kafka. Topic: "
                            + topic + ", key: " + key,
                    ex
            );
        }
    }
}
