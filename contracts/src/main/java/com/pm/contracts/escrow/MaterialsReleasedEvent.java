package com.pm.contracts.escrow;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MaterialsReleasedEvent(
        UUID eventId,
        int eventVersion,
        Instant occurredAt,
        UUID escrowId,
        UUID paymentId,
        UUID bookingId,
        UUID quoteId,
        UUID customerId,
        UUID providerId,
        BigDecimal amount
) implements DomainEvent {

    @Override
    public String eventType() {
        return "MATERIALS_RELEASED";
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
