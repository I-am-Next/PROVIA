package com.pm.contracts.payment;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID paymentId,

        UUID bookingId,

        UUID customerId,

        UUID providerId,

        BigDecimal amount,

        String currency,

        String reason

) implements DomainEvent {

    @Override
    public String eventType() {

        return "PAYMENT_FAILED";

    }

    @Override
    public UUID aggregateId() {

        return paymentId;

    }

    @Override
    public String aggregateType() {

        return "PAYMENT";

    }

}
