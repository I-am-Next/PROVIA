package com.pm.contracts.catalog;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CategoryDeletedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID categoryId

) implements DomainEvent {

    @Override
    public String eventType() {
        return "CATEGORY_DELETED";
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