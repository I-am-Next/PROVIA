package com.pm.paymentservicenew.integration.producer;

import com.pm.paymentservicenew.config.PaymentKafkaProperties;
import com.pm.paymentservicenew.infrastructure.outbox.OutboxEventType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KafkaTopicResolverTest {

    private static final String PAYMENTS_TOPIC = "payment.events";
    private static final String ESCROWS_TOPIC = "escrow.events";
    private static final String WALLETS_TOPIC = "wallet.events";

    private final KafkaTopicResolver resolver =
            new KafkaTopicResolver(
                    new PaymentKafkaProperties(
                            PAYMENTS_TOPIC,
                            ESCROWS_TOPIC,
                            WALLETS_TOPIC
                    )
            );

    @Test
    void shouldRouteWithdrawalCompletedToWalletTopic() {
        assertEquals(
                WALLETS_TOPIC,
                resolver.resolve(OutboxEventType.WITHDRAWAL_COMPLETED)
        );
    }

    @Test
    void shouldRouteWithdrawalFailedToWalletTopic() {
        assertEquals(
                WALLETS_TOPIC,
                resolver.resolve(OutboxEventType.WITHDRAWAL_FAILED)
        );
    }

    @Test
    void shouldRouteWalletWithdrawalCompletedToWalletTopic() {
        assertEquals(
                WALLETS_TOPIC,
                resolver.resolve(
                        OutboxEventType.WALLET_WITHDRAWAL_COMPLETED
                )
        );
    }

    @Test
    void shouldRouteWalletWithdrawalCancelledToWalletTopic() {
        assertEquals(
                WALLETS_TOPIC,
                resolver.resolve(
                        OutboxEventType.WALLET_WITHDRAWAL_CANCELLED
                )
        );
    }

    @Test
    void shouldKeepPaymentEventsOnPaymentsTopic() {
        assertEquals(
                PAYMENTS_TOPIC,
                resolver.resolve(OutboxEventType.PAYMENT_COMPLETED)
        );
    }

    @Test
    void shouldKeepEscrowEventsOnEscrowsTopic() {
        assertEquals(
                ESCROWS_TOPIC,
                resolver.resolve(OutboxEventType.ESCROW_CREATED)
        );
    }

    @Test
    void shouldKeepExistingWithdrawalRequestedOnWalletTopic() {
        assertEquals(
                WALLETS_TOPIC,
                resolver.resolve(OutboxEventType.WITHDRAWAL_REQUESTED)
        );
    }

    @Test
    void shouldRejectNullEventType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(null)
        );
    }
}