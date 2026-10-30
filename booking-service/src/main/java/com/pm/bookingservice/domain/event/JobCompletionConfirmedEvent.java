package com.pm.bookingservice.domain.event;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record JobCompletionConfirmedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID bookingId,

        UUID customerId,

        UUID providerId

) implements DomainEvent {

    @Override
    public String eventType() {
        return "JOB_COMPLETION_CONFIRMED";
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