package com.pm.contracts.provider;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ProviderServiceDeletedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID providerServiceId,

        UUID providerId,

        UUID serviceItemId

) implements DomainEvent {

    @Override
    public String eventType() {

        return "PROVIDER_SERVICE_DELETED";
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