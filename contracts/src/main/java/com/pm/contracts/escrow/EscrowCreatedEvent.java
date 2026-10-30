package com.pm.contracts.escrow;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EscrowCreatedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID escrowId,

        UUID paymentId,

        UUID bookingId,

        UUID customerId,

        UUID providerId,

        BigDecimal totalAmount,

        BigDecimal releasedAmount,

        String escrowStatus,

        Instant createdAt

) implements DomainEvent {

    @Override
    public String eventType() {

        return "ESCROW_CREATED";

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