package com.pm.notificationservice.infrastructure.consumer.payment;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.payment.PaymentCompletedEvent;
import com.pm.contracts.payment.PaymentFailedEvent;
import com.pm.contracts.payment.PaymentPendingEvent;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventTypeResolver {

    public boolean supports(
            String eventType
    ) {

        return switch (eventType) {

            case "PAYMENT_PENDING",
                 "PAYMENT_COMPLETED",
                 "PAYMENT_FAILED" -> true;

            default -> false;
        };
    }

    public Class<? extends DomainEvent> resolve(
            String eventType
    ) {

        return switch (eventType) {

            case "PAYMENT_PENDING" ->
                    PaymentPendingEvent.class;

            case "PAYMENT_COMPLETED" ->
                    PaymentCompletedEvent.class;

            case "PAYMENT_FAILED" ->
                    PaymentFailedEvent.class;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported payment event type: "
                                    + eventType
                    );
        };
    }
}