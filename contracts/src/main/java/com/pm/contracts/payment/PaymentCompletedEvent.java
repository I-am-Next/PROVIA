package com.pm.contracts.payment;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public record PaymentCompletedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID paymentId,

        UUID bookingId,

        UUID escrowId,

        UUID customerId,

        UUID providerId,

        BigDecimal amount,

        String currency,

        String gatewayReference,

        String paymentStatus,

        Instant completedAt

) implements DomainEvent {


    @Override
    public String eventType() {

        return "PAYMENT_COMPLETED";

    }


    @Override
    public String aggregateType() {

        return "PAYMENT";

    }


    @Override
    public UUID aggregateId() {

        return paymentId;

    }

}