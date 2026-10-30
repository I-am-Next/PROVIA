package com.pm.notificationservice.infrastructure.consumer.rating;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.rating.CustomerRatedEvent;
import com.pm.contracts.rating.ProviderRatedEvent;
import org.springframework.stereotype.Component;

@Component
public class RatingEventTypeResolver {

    public boolean supports(
            String eventType
    ) {

        return switch (eventType) {

            case "CUSTOMER_RATED",
                 "PROVIDER_RATED" -> true;

            default -> false;
        };
    }

    public Class<? extends DomainEvent> resolve(
            String eventType
    ) {

        return switch (eventType) {

            case "CUSTOMER_RATED" ->
                    CustomerRatedEvent.class;

            case "PROVIDER_RATED" ->
                    ProviderRatedEvent.class;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported rating event type: "
                                    + eventType
                    );
        };
    }
}