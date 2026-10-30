package com.pm.notificationservice.infrastructure.consumer.quote;

import com.pm.contracts.booking.QuoteAcceptedEvent;
import com.pm.contracts.booking.QuoteCreatedEvent;
import com.pm.contracts.booking.QuoteReceivedEvent;
import com.pm.contracts.booking.QuoteRejectedEvent;
import com.pm.contracts.common.DomainEvent;
import org.springframework.stereotype.Component;

@Component
public class QuoteEventResolver {

    public boolean supports(String eventType) {
        return switch (eventType) {
            case "QUOTE_CREATED",
                 "QUOTE_RECEIVED",
                 "QUOTE_ACCEPTED",
                 "QUOTE_REJECTED" -> true;

            default -> false;
        };
    }

    public Class<? extends DomainEvent> resolve(String eventType) {
        return switch (eventType) {
            case "QUOTE_CREATED" ->
                    QuoteCreatedEvent.class;

            case "QUOTE_RECEIVED" ->
                    QuoteReceivedEvent.class;

            case "QUOTE_ACCEPTED" ->
                    QuoteAcceptedEvent.class;

            case "QUOTE_REJECTED" ->
                    QuoteRejectedEvent.class;

            default -> throw new IllegalArgumentException(
                    "Unsupported quote event type: " + eventType
            );
        };
    }
}
