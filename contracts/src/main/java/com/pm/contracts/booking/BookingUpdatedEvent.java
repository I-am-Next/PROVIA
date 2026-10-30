package com.pm.contracts.booking;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record BookingUpdatedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID bookingId,

        UUID customerId,

        UUID providerId

) implements DomainEvent {

    @Override
    public String eventType() {

        return "BOOKING_UPDATED";

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
