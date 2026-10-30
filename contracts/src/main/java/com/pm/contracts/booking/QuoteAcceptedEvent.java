package com.pm.contracts.booking;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record QuoteAcceptedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID quoteId,

        UUID bookingId,

        UUID customerId,

        UUID providerId

) implements DomainEvent {

    @Override
    public String eventType() {

        return "QUOTE_ACCEPTED";

    }

    @Override
    public UUID aggregateId() {

        return quoteId;

    }

    @Override
    public String aggregateType() {

        return "QUOTE";

    }

}