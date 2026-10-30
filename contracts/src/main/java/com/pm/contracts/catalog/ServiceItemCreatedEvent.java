package com.pm.contracts.catalog;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ServiceItemCreatedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID serviceItemId,

        UUID categoryId,

        String name,

        String description,

        boolean active

) implements DomainEvent {

    @Override
    public String eventType() {
        return "SERVICE_ITEM_CREATED";
    }

    @Override
    public String aggregateType() {
        return "SERVICE_ITEM";
    }

    @Override
    public UUID aggregateId() {
        return serviceItemId;
    }
}