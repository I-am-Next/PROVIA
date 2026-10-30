package com.pm.providerservice.application.mapper;

import com.pm.contracts.provider.ProviderServiceCreatedEvent;
import com.pm.contracts.provider.ProviderServiceDeletedEvent;
import com.pm.contracts.provider.ProviderServiceUpdatedEvent;
import com.pm.providerservice.domain.ProviderService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ProviderServiceEventMapper {

    public ProviderServiceCreatedEvent toCreatedEvent(
            ProviderService service
    ) {

        return new ProviderServiceCreatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                service.getId(),
                service.getProviderProfileId(),
                service.getServiceItemId(),
                service.getPrice(),
                service.isActive()
        );
    }

    public ProviderServiceUpdatedEvent toUpdatedEvent(
            ProviderService service
    ) {

        return new ProviderServiceUpdatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                service.getId(),
                service.getProviderProfileId(),
                service.getServiceItemId(),
                service.getPrice(),
                service.isActive()
        );
    }

    public ProviderServiceDeletedEvent toDeletedEvent(
            ProviderService service
    ) {

        return new ProviderServiceDeletedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                service.getId(),
                service.getProviderProfileId(),
                service.getServiceItemId()
        );
    }
}