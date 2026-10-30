package com.pm.paymentservicenew.infrastructure.outbox;

import com.pm.contracts.common.DomainEvent;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventTypeResolver {

    public OutboxEventType resolve(DomainEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Domain event must not be null."
            );
        }

        return switch (event.eventType()) {

            /*
             * Payment events
             */
            case "PAYMENT_COMPLETED" ->
                    OutboxEventType.PAYMENT_COMPLETED;

            case "PAYMENT_FAILED" ->
                    OutboxEventType.PAYMENT_FAILED;

            case "PAYMENT_REFUNDED",
                 "REFUND_COMPLETED" ->
                    OutboxEventType.PAYMENT_REFUNDED;

            case "PAYMENT_EXPIRED" ->
                    OutboxEventType.PAYMENT_EXPIRED;

            /*
             * Escrow events
             */
            case "ESCROW_CREATED" ->
                    OutboxEventType.ESCROW_CREATED;

            case "MATERIALS_RELEASED" ->
                    OutboxEventType.MATERIALS_RELEASED;

            case "ESCROW_RELEASED" ->
                    OutboxEventType.ESCROW_RELEASED;

            case "LABOUR_RELEASED" ->
                    OutboxEventType.LABOUR_RELEASED;

            /*
             * Wallet events
             */
            case "WALLET_CREDITED" ->
                    OutboxEventType.WALLET_CREDITED;

            case "WALLET_DEBITED" ->
                    OutboxEventType.WALLET_DEBITED;

            case "WALLET_FUNDS_RESERVED" ->
                    OutboxEventType.WALLET_FUNDS_RESERVED;

            case "WITHDRAWAL_REQUESTED" ->
                    OutboxEventType.WITHDRAWAL_REQUESTED;

            case "WITHDRAWAL_COMPLETED" ->
                    OutboxEventType.WITHDRAWAL_COMPLETED;

            case "WITHDRAWAL_FAILED" ->
                    OutboxEventType.WITHDRAWAL_FAILED;

            case "WALLET_WITHDRAWAL_COMPLETED" ->
                    OutboxEventType.WALLET_WITHDRAWAL_COMPLETED;

            case "WALLET_WITHDRAWAL_CANCELLED" ->
                    OutboxEventType.WALLET_WITHDRAWAL_CANCELLED;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported event type: "
                                    + event.eventType()
                    );
        };
    }
}