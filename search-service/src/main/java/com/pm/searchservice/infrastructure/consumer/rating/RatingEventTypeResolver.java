package com.pm.searchservice.infrastructure.consumer.rating;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.rating.CustomerRatedEvent;
import com.pm.contracts.rating.CustomerRatingDeletedEvent;
import com.pm.contracts.rating.CustomerRatingUpdatedEvent;
import org.springframework.stereotype.Component;

@Component
public class RatingEventTypeResolver {

    public boolean supports(
            String eventType
    ) {

        return switch (eventType) {

            case "CUSTOMER_RATED",
                 "CUSTOMER_RATING_UPDATED",
                 "CUSTOMER_RATING_DELETED" ->
                    true;

            default ->
                    false;
        };
    }

    public Class<? extends DomainEvent> resolve(
            String eventType
    ) {

        return switch (eventType) {

            case "CUSTOMER_RATED" ->
                    CustomerRatedEvent.class;

            case "CUSTOMER_RATING_UPDATED" ->
                    CustomerRatingUpdatedEvent.class;

            case "CUSTOMER_RATING_DELETED" ->
                    CustomerRatingDeletedEvent.class;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported rating event type: "
                                    + eventType
                    );
        };
    }
}