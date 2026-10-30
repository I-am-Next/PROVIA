package com.pm.contracts.catalog;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CategoryCreatedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID categoryId,

        String name,

        String description,

        boolean active

) implements DomainEvent {

    @Override
    public String eventType() {
        return "CATEGORY_CREATED";
    }

    @Override
    public String aggregateType() {
        return "CATEGORY";
    }

    @Override
    public UUID aggregateId() {
        return categoryId;
    }
}