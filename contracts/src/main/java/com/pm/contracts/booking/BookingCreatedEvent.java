package com.pm.contracts.booking;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record BookingCreatedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID bookingId,

        UUID customerId,

        UUID providerId,

        UUID providerServiceId

) implements DomainEvent {

    @Override
    public String eventType() {
        return "BOOKING_CREATED";
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