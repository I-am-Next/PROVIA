package com.pm.contracts.escrow;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LabourReleasedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID escrowId,

        UUID paymentId,

        UUID bookingId,

        UUID customerId,

        UUID providerId,

        BigDecimal amount

) implements DomainEvent {

    @Override
    public String eventType() {

        return "LABOUR_RELEASED";

    }

    @Override
    public UUID aggregateId() {

        return escrowId;

    }

    @Override
    public String aggregateType() {

        return "ESCROW";

    }

}