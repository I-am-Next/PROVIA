package com.pm.contracts.catalog;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ServiceItemDeletedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID serviceItemId,

        UUID categoryId

) implements DomainEvent {

    @Override
    public String eventType() {
        return "SERVICE_ITEM_DELETED";
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