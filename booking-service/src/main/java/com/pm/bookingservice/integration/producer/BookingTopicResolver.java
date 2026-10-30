package com.pm.bookingservice.integration.producer;

import com.pm.bookingservice.integration.kafka.BookingTopics;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class BookingTopicResolver {

    public String resolve(String aggregateType) {

        if (aggregateType == null || aggregateType.isBlank()) {
            throw new IllegalArgumentException(
                    "Outbox aggregate type must not be blank."
            );
        }

        return switch (aggregateType.trim().toUpperCase(Locale.ROOT)) {
            case "BOOKING" -> BookingTopics.BOOKING_EVENTS;
            case "QUOTE" -> BookingTopics.QUOTE_EVENTS;

            default -> throw new IllegalArgumentException(
                    "No Kafka topic configured for aggregate type: "
                            + aggregateType
            );
        };
    }
}
