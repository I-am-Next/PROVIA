package com.pm.paymentservicenew.infrastructure.outbox;

import com.pm.contracts.common.DomainEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutboxEventTypeResolverTest {

    private final OutboxEventTypeResolver resolver =
            new OutboxEventTypeResolver();

    @Test
    void shouldResolveWithdrawalCompleted() {
        assertResolves(
                "WITHDRAWAL_COMPLETED",
                OutboxEventType.WITHDRAWAL_COMPLETED
        );
    }

    @Test
    void shouldResolveWithdrawalFailed() {
        assertResolves(
                "WITHDRAWAL_FAILED",
                OutboxEventType.WITHDRAWAL_FAILED
        );
    }

    @Test
    void shouldResolveWalletWithdrawalCompleted() {
        assertResolves(
                "WALLET_WITHDRAWAL_COMPLETED",
                OutboxEventType.WALLET_WITHDRAWAL_COMPLETED
        );
    }

    @Test
    void shouldResolveWalletWithdrawalCancelled() {
        assertResolves(
                "WALLET_WITHDRAWAL_CANCELLED",
                OutboxEventType.WALLET_WITHDRAWAL_CANCELLED
        );
    }

    @Test
    void shouldPreserveExistingWithdrawalRequestedMapping() {
        assertResolves(
                "WITHDRAWAL_REQUESTED",
                OutboxEventType.WITHDRAWAL_REQUESTED
        );
    }

    @Test
    void shouldRejectUnsupportedEventType() {
        DomainEvent event = mock(DomainEvent.class);
        when(event.eventType()).thenReturn("UNKNOWN_EVENT");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(event)
        );

        assertEquals(
                "Unsupported event type: UNKNOWN_EVENT",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(null)
        );
    }

    private void assertResolves(
            String eventType,
            OutboxEventType expected
    ) {
        DomainEvent event = mock(DomainEvent.class);
        when(event.eventType()).thenReturn(eventType);

        assertEquals(expected, resolver.resolve(event));
    }
}