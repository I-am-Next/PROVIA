package com.pm.contracts.provider;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ProviderDeletedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID providerId

) implements DomainEvent {

    @Override
    public String eventType() {

        return "PROVIDER_DELETED";
    }

    @Override
    public UUID aggregateId() {

        return providerId;
    }

    @Override
    public String aggregateType() {

        return "PROVIDER";
    }
}