package com.pm.ratingservice.integration.producer;

import com.pm.ratingservice.infrastructure.outbox.OutboxEventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopicResolver {

    private final String ratingEventsTopic;

    public KafkaTopicResolver(
            @Value("${rating.kafka.events-topic}")
            String ratingEventsTopic
    ) {
        if (ratingEventsTopic == null || ratingEventsTopic.isBlank()) {
            throw new IllegalArgumentException(
                    "rating.kafka.events-topic must not be blank"
            );
        }

        this.ratingEventsTopic = ratingEventsTopic;
    }

    public String resolve(OutboxEventType eventType) {

        if (eventType == null) {
            throw new IllegalArgumentException(
                    "Rating event type must not be null"
            );
        }

        return switch (eventType) {
            case CUSTOMER_RATED,
                 CUSTOMER_RATING_UPDATED,
                 CUSTOMER_RATING_DELETED,
                 PROVIDER_RATED -> ratingEventsTopic;
        };
    }
}