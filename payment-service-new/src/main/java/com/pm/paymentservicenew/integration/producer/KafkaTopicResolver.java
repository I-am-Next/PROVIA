package com.pm.paymentservicenew.integration.producer;

import com.pm.paymentservicenew.config.PaymentKafkaProperties;
import com.pm.paymentservicenew.infrastructure.outbox.OutboxEventType;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopicResolver {

    private final PaymentKafkaProperties properties;

    public KafkaTopicResolver(PaymentKafkaProperties properties) {
        this.properties = properties;
    }

    public String resolve(OutboxEventType eventType) {

        if (eventType == null) {
            throw new IllegalArgumentException(
                    "Outbox event type must not be null."
            );
        }

        return switch (eventType) {

            /*
             * Payment domain events
             */
            case PAYMENT_COMPLETED,
                 PAYMENT_FAILED,
                 PAYMENT_REFUNDED,
                 PAYMENT_EXPIRED ->
                    properties.payments();

            /*
             * Escrow domain events
             */
            case ESCROW_CREATED,
                 MATERIALS_RELEASED,
                 LABOUR_RELEASED,
                 ESCROW_RELEASED ->
                    properties.escrows();

            /*
             * Wallet and withdrawal domain events
             */
            case WALLET_CREDITED,
                 WALLET_DEBITED,
                 WALLET_FUNDS_RESERVED,
                 WITHDRAWAL_REQUESTED,
                 WITHDRAWAL_COMPLETED,
                 WITHDRAWAL_FAILED,
                 WALLET_WITHDRAWAL_COMPLETED,
                 WALLET_WITHDRAWAL_CANCELLED ->
                    properties.wallets();
        };
    }
}