package com.pm.paymentservicenew.application.factory;

import com.pm.contracts.escrow.EscrowCreatedEvent;
import com.pm.contracts.payment.PaymentCompletedEvent;
import com.pm.paymentservicenew.domain.model.escrow.Escrow;
import com.pm.paymentservicenew.domain.model.payment.Payment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentEventFactory {

    public PaymentCompletedEvent paymentCompleted(
            Payment payment,
            Escrow escrow
    ) {

        return new PaymentCompletedEvent(

                UUID.randomUUID(),

                1,

                payment.getCompletedAt(),

                payment.getId(),

                payment.getBookingId(),

                escrow.getId(),

                payment.getCustomerId(),

                payment.getProviderId(),

                payment.getAmount(),

                payment.getCurrency(),

                payment.getGatewayReference(),

                payment.getStatus().name(),

                payment.getCompletedAt()

        );

    }

    public EscrowCreatedEvent escrowCreated(
            Escrow escrow
    ) {

        return new EscrowCreatedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                escrow.getId(),

                escrow.getPaymentId(),

                escrow.getBookingId(),

                escrow.getCustomerId(),

                escrow.getProviderId(),

                escrow.getTotalAmount(),

                escrow.getReleasedAmount(),

                escrow.getStatus().name(),

                escrow.getCreatedAt()

        );

    }

}