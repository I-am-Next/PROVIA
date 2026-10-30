package com.pm.notificationservice.infrastructure.consumer;

import com.pm.contracts.booking.BookingCompletedEvent;
import com.pm.contracts.booking.BookingStartedEvent;
import com.pm.contracts.booking.QuoteCreatedEvent;
import com.pm.contracts.escrow.EscrowRefundedEvent;
import com.pm.contracts.wallet.WalletWithdrawalCancelledEvent;
import com.pm.contracts.wallet.WalletWithdrawalCompletedEvent;
import com.pm.notificationservice.infrastructure.consumer.booking.EventTypeResolver;
import com.pm.notificationservice.infrastructure.consumer.escrow.EscrowEventTypeResolver;
import com.pm.notificationservice.infrastructure.consumer.quote.QuoteEventResolver;
import com.pm.notificationservice.infrastructure.consumer.wallet.WalletEventTypeResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanonicalEventTypeResolverTest {

    private final EventTypeResolver bookingResolver =
            new EventTypeResolver();

    private final QuoteEventResolver quoteResolver =
            new QuoteEventResolver();

    private final EscrowEventTypeResolver escrowResolver =
            new EscrowEventTypeResolver();

    private final WalletEventTypeResolver walletResolver =
            new WalletEventTypeResolver();

    @Test
    void bookingStarted_shouldResolveCanonicalContract() {
        String eventType = "BOOKING_STARTED";

        assertTrue(
                bookingResolver.supports(eventType),
                "Notification must recognize BOOKING_STARTED"
        );

        assertEquals(
                BookingStartedEvent.class,
                bookingResolver.resolve(eventType)
        );
    }

    @Test
    void bookingCompleted_shouldResolveCanonicalContract() {
        String eventType = "BOOKING_COMPLETED";

        assertTrue(
                bookingResolver.supports(eventType),
                "Notification must recognize BOOKING_COMPLETED"
        );

        assertEquals(
                BookingCompletedEvent.class,
                bookingResolver.resolve(eventType)
        );
    }

    @Test
    void quoteCreated_shouldResolveCanonicalContract() {
        String eventType = "QUOTE_CREATED";

        assertTrue(
                quoteResolver.supports(eventType),
                "Notification must recognize QUOTE_CREATED"
        );

        assertEquals(
                QuoteCreatedEvent.class,
                quoteResolver.resolve(eventType)
        );
    }

    @Test
    void escrowRefunded_shouldResolveCanonicalContract() {
        String eventType = "ESCROW_REFUNDED";

        assertTrue(
                escrowResolver.supports(eventType),
                "Notification must recognize ESCROW_REFUNDED"
        );

        assertEquals(
                EscrowRefundedEvent.class,
                escrowResolver.resolve(eventType)
        );
    }

    @Test
    void walletWithdrawalCompleted_shouldResolveCanonicalContract() {
        String eventType = "WALLET_WITHDRAWAL_COMPLETED";

        assertTrue(
                walletResolver.supports(eventType),
                "Notification must recognize WALLET_WITHDRAWAL_COMPLETED"
        );

        assertEquals(
                WalletWithdrawalCompletedEvent.class,
                walletResolver.resolve(eventType)
        );
    }

    @Test
    void walletWithdrawalCancelled_shouldResolveCanonicalContract() {
        String eventType = "WALLET_WITHDRAWAL_CANCELLED";

        assertTrue(
                walletResolver.supports(eventType),
                "Notification must recognize WALLET_WITHDRAWAL_CANCELLED"
        );

        assertEquals(
                WalletWithdrawalCancelledEvent.class,
                walletResolver.resolve(eventType)
        );
    }
}
