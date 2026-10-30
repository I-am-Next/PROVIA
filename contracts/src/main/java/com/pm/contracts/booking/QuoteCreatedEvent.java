package com.pm.contracts.booking;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record QuoteCreatedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID quoteId,

        UUID bookingId,

        UUID providerId

) implements DomainEvent {

    @Override
    public String eventType() {
        return "QUOTE_CREATED";
    }

    @Override
    public String aggregateType() {
        return "QUOTE";
    }

    @Override
    public UUID aggregateId() {
        return quoteId;
    }

}