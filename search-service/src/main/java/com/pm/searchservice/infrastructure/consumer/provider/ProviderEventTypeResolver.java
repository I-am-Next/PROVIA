package com.pm.searchservice.infrastructure.consumer.provider;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.provider.ProviderCreatedEvent;
import com.pm.contracts.provider.ProviderDeletedEvent;
import com.pm.contracts.provider.ProviderUpdatedEvent;
import org.springframework.stereotype.Component;

@Component
public class ProviderEventTypeResolver {

    public boolean supports(String eventType) {

        return switch (eventType) {

            case "PROVIDER_CREATED",
                 "PROVIDER_UPDATED",
                 "PROVIDER_DELETED" -> true;

            default -> false;
        };
    }

    public Class<? extends DomainEvent> resolve(
            String eventType
    ) {

        return switch (eventType) {

            case "PROVIDER_CREATED" ->
                    ProviderCreatedEvent.class;

            case "PROVIDER_UPDATED" ->
                    ProviderUpdatedEvent.class;

            case "PROVIDER_DELETED" ->
                    ProviderDeletedEvent.class;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported provider event type: "
                                    + eventType
                    );
        };
    }
}