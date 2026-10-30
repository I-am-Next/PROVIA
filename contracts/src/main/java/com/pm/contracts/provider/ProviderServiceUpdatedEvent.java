package com.pm.contracts.provider;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProviderServiceUpdatedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID providerServiceId,

        UUID providerId,

        UUID serviceItemId,

        BigDecimal price,

        boolean active

) implements DomainEvent {

    @Override
    public String eventType() {

        return "PROVIDER_SERVICE_UPDATED";
    }

    @Override
    public UUID aggregateId() {

        return providerServiceId;
    }

    @Override
    public String aggregateType() {

        return "PROVIDER_SERVICE";
    }
}