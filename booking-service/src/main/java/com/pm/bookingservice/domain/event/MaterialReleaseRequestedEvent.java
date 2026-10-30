package com.pm.bookingservice.domain.event;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MaterialReleaseRequestedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID bookingId,

        UUID providerId,

        UUID quoteId,

        BigDecimal materialsAmount

) implements DomainEvent {

    @Override
    public String eventType() {
        return "MATERIAL_RELEASE_REQUESTED";
    }

    @Override
    public String aggregateType() {
        return "BOOKING";
    }

    @Override
    public UUID aggregateId() {
        return bookingId;
    }

}