package com.pm.ratingservice.infrastructure.outbox;

import com.pm.contracts.common.DomainEvent;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventTypeResolver {

    public OutboxEventType resolve(
            DomainEvent event
    ) {

        return switch (event.eventType()) {

            case "CUSTOMER_RATED" ->
                    OutboxEventType.CUSTOMER_RATED;

            case "CUSTOMER_RATING_UPDATED" ->
                    OutboxEventType.CUSTOMER_RATING_UPDATED;

            case "CUSTOMER_RATING_DELETED" ->
                    OutboxEventType.CUSTOMER_RATING_DELETED;

            case "PROVIDER_RATED" ->
                    OutboxEventType.PROVIDER_RATED;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported rating event type: "
                                    + event.eventType()
                    );
        };
    }
}