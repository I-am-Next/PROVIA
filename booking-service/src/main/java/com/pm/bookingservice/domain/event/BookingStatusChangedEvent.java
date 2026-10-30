package com.pm.bookingservice.domain.event;

import com.pm.bookingservice.domain.model.BookingStatus;
import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record BookingStatusChangedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID bookingId,

        UUID customerId,

        UUID providerId,

        BookingStatus previousStatus,

        BookingStatus currentStatus

) implements DomainEvent {

    @Override
    public String eventType() {
        return "BOOKING_STATUS_CHANGED";
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