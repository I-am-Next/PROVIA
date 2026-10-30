package com.pm.searchservice.infrastructure.consumer.providerservice;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.provider.ProviderServiceCreatedEvent;
import com.pm.contracts.provider.ProviderServiceDeletedEvent;
import com.pm.contracts.provider.ProviderServiceUpdatedEvent;
import org.springframework.stereotype.Component;

@Component
public class ProviderServiceEventTypeResolver {

    public boolean supports(String eventType) {

        return switch (eventType) {

            case "PROVIDER_SERVICE_CREATED",
                 "PROVIDER_SERVICE_UPDATED",
                 "PROVIDER_SERVICE_DELETED" -> true;

            default -> false;
        };
    }

    public Class<? extends DomainEvent> resolve(
            String eventType
    ) {

        return switch (eventType) {

            case "PROVIDER_SERVICE_CREATED" ->
                    ProviderServiceCreatedEvent.class;

            case "PROVIDER_SERVICE_UPDATED" ->
                    ProviderServiceUpdatedEvent.class;

            case "PROVIDER_SERVICE_DELETED" ->
                    ProviderServiceDeletedEvent.class;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported provider service event type: "
                                    + eventType
                    );
        };
    }
}