package com.pm.bookingservice.domain.model;

import com.pm.bookingservice.domain.exception.InvalidBookingStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PROVIDER_USER_ID = UUID.randomUUID();
    private static final UUID PROVIDER_SERVICE_ID = UUID.randomUUID();

    private static final String SERVICE_NAME = "Plumbing Repair";
    private static final String DESCRIPTION = "Repair leaking kitchen pipe";
    private static final String NOTES = "Customer prefers morning appointment";

    private static final BigDecimal PRICE =
            new BigDecimal("150.00");

    private final BookingAddress address =
            createAddress();

    private final LocalDateTime start =
            LocalDateTime.now().plusDays(2);

    private final LocalDateTime end =
            start.plusHours(2);

    // ---------------------------------------------------------
    // Creation
    // ---------------------------------------------------------

    @Test
    void shouldCreateBookingWithValidData() {

        Booking booking = createValidBooking();

        assertNotNull(booking);

        assertEquals(USER_ID, booking.getUserId());
        assertEquals(PROVIDER_USER_ID, booking.getProviderUserId());
        assertEquals(PROVIDER_SERVICE_ID, booking.getProviderServiceId());

        assertEquals(SERVICE_NAME, booking.getServiceName());
        assertEquals(PRICE, booking.getPrice());
        assertEquals(DESCRIPTION, booking.getDescription());
        assertEquals(NOTES, booking.getNotes());
        assertEquals(address, booking.getAddress());

        assertEquals(start, booking.getScheduledStart());
        assertEquals(end, booking.getScheduledEnd());

        assertEquals(
                BookingStatus.REQUESTED,
                booking.getStatus()
        );
    }

    @Test
    void shouldRejectNullUserId() {

        assertThrows(
                NullPointerException.class,
                () -> Booking.create(
                        null,
                        PROVIDER_USER_ID,
                        PROVIDER_SERVICE_ID,
                        SERVICE_NAME,
                        PRICE,
                        DESCRIPTION,
                        NOTES,
                        address,
                        start,
                        end
                )
        );
    }

    @Test
    void shouldRejectNullProviderUserId() {

        assertThrows(
                NullPointerException.class,
                () -> Booking.create(
                        USER_ID,
                        null,
                        PROVIDER_SERVICE_ID,
                        SERVICE_NAME,
                        PRICE,
                        DESCRIPTION,
                        NOTES,
                        address,
                        start,
                        end
                )
        );
    }

    @Test
    void shouldRejectNullProviderServiceId() {

        assertThrows(
                NullPointerException.class,
                () -> Booking.create(
                        USER_ID,
                        PROVIDER_USER_ID,
                        null,
                        SERVICE_NAME,
                        PRICE,
                        DESCRIPTION,
                        NOTES,
                        address,
                        start,
                        end
                )
        );
    }

    @Test
    void shouldRejectBlankServiceName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> Booking.create(
                        USER_ID,
                        PROVIDER_USER_ID,
                        PROVIDER_SERVICE_ID,
                        "   ",
                        PRICE,
                        DESCRIPTION,
                        NOTES,
                        address,
                        start,
                        end
                )
        );
    }

    @Test
    void shouldRejectNegativePrice() {

        assertThrows(
                IllegalArgumentException.class,
                () -> Booking.create(
                        USER_ID,
                        PROVIDER_USER_ID,
                        PROVIDER_SERVICE_ID,
                        SERVICE_NAME,
                        new BigDecimal("-1.00"),
                        DESCRIPTION,
                        NOTES,
                        address,
                        start,
                        end
                )
        );
    }

    @Test
    void shouldRejectNullPrice() {

        assertThrows(
                NullPointerException.class,
                () -> Booking.create(
                        USER_ID,
                        PROVIDER_USER_ID,
                        PROVIDER_SERVICE_ID,
                        SERVICE_NAME,
                        null,
                        DESCRIPTION,
                        NOTES,
                        address,
                        start,
                        end
                )
        );
    }

    // ---------------------------------------------------------
    // Schedule validation
    // ---------------------------------------------------------

    @Test
    void shouldRejectNullScheduledStart() {

        assertThrows(
                NullPointerException.class,
                () -> Booking.create(
                        USER_ID,
                        PROVIDER_USER_ID,
                        PROVIDER_SERVICE_ID,
                        SERVICE_NAME,
                        PRICE,
                        DESCRIPTION,
                        NOTES,
                        address,
                        null,
                        end
                )
        );
    }

    @Test
    void shouldRejectNullScheduledEnd() {

        assertThrows(
                NullPointerException.class,
                () -> Booking.create(
                        USER_ID,
                        PROVIDER_USER_ID,
                        PROVIDER_SERVICE_ID,
                        SERVICE_NAME,
                        PRICE,
                        DESCRIPTION,
                        NOTES,
                        address,
                        start,
                        null
                )
        );
    }

    @Test
    void shouldRejectEndBeforeStart() {

        LocalDateTime invalidEnd =
                start.minusMinutes(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> Booking.create(
                        USER_ID,
                        PROVIDER_USER_ID,
                        PROVIDER_SERVICE_ID,
                        SERVICE_NAME,
                        PRICE,
                        DESCRIPTION,
                        NOTES,
                        address,
                        start,
                        invalidEnd
                )
        );
    }

    @Test
    void shouldRejectEndEqualToStart() {

        assertThrows(
                IllegalArgumentException.class,
                () -> Booking.create(
                        USER_ID,
                        PROVIDER_USER_ID,
                        PROVIDER_SERVICE_ID,
                        SERVICE_NAME,
                        PRICE,
                        DESCRIPTION,
                        NOTES,
                        address,
                        start,
                        start
                )
        );
    }

    // ---------------------------------------------------------
    // Booking lifecycle
    // ---------------------------------------------------------

    @Test
    void shouldAcceptRequestedBooking() {

        Booking booking = createValidBooking();

        booking.accept();

        assertEquals(
                BookingStatus.AWAITING_QUOTE,
                booking.getStatus()
        );

        assertNotNull(booking.getAcceptedAt());
    }

    @Test
    void shouldRejectRequestedBooking() {

        Booking booking = createValidBooking();

        booking.reject();

        assertEquals(
                BookingStatus.REJECTED,
                booking.getStatus()
        );
    }

    @Test
    void shouldSendQuoteAfterBookingIsAccepted() {

        Booking booking = createValidBooking();

        booking.accept();
        booking.quoteSent();

        assertEquals(
                BookingStatus.QUOTE_SENT,
                booking.getStatus()
        );
    }

    @Test
    void shouldAcceptQuote() {

        Booking booking = createValidBooking();

        booking.accept();
        booking.quoteSent();

        BigDecimal quotedPrice =
                new BigDecimal("175.00");

        booking.quoteAccepted(quotedPrice);

        assertEquals(
                BookingStatus.QUOTE_ACCEPTED,
                booking.getStatus()
        );

        assertEquals(
                quotedPrice,
                booking.getPrice()
        );
    }

    @Test
    void shouldRejectNegativeQuotedPrice() {

        Booking booking = createValidBooking();

        booking.accept();
        booking.quoteSent();

        assertThrows(
                IllegalArgumentException.class,
                () -> booking.quoteAccepted(
                        new BigDecimal("-50.00")
                )
        );
    }

    @Test
    void shouldRejectQuoteAfterBookingIsNotAwaitingQuote() {

        Booking booking = createValidBooking();

        assertThrows(
                InvalidBookingStateException.class,
                booking::quoteSent
        );
    }

    @Test
    void shouldCompleteNormalBookingLifecycle() {

        Booking booking = createValidBooking();

        booking.accept();
        booking.quoteSent();
        booking.quoteAccepted(
                new BigDecimal("175.00")
        );
        booking.paymentCompleted();
        booking.materialsReleased();
        booking.start();
        booking.complete();
        booking.confirmCompletion();

        assertEquals(
                BookingStatus.COMPLETION_CONFIRMED,
                booking.getStatus()
        );

        assertNotNull(booking.getStartedAt());
        assertNotNull(booking.getCompletedAt());
    }

    // ---------------------------------------------------------
    // Invalid state transitions
    // ---------------------------------------------------------

    @Test
    void shouldNotCompleteBookingBeforeItStarts() {

        Booking booking = createValidBooking();

        assertThrows(
                InvalidBookingStateException.class,
                booking::complete
        );
    }

    @Test
    void shouldNotStartBookingBeforeMaterialsAreReleased() {

        Booking booking = createValidBooking();

        assertThrows(
                InvalidBookingStateException.class,
                booking::start
        );
    }

    @Test
    void shouldNotReleaseMaterialsBeforePayment() {

        Booking booking = createValidBooking();

        assertThrows(
                InvalidBookingStateException.class,
                booking::materialsReleased
        );
    }

    @Test
    void shouldNotCompleteConfirmationBeforeCompletion() {

        Booking booking = createValidBooking();

        assertThrows(
                InvalidBookingStateException.class,
                booking::confirmCompletion
        );
    }

    // ---------------------------------------------------------
    // Cancellation
    // ---------------------------------------------------------

    @Test
    void shouldCancelRequestedBooking() {

        Booking booking = createValidBooking();

        booking.cancel();

        assertEquals(
                BookingStatus.CANCELLED,
                booking.getStatus()
        );

        assertNotNull(booking.getCancelledAt());
    }

    @Test
    void shouldCancelBookingAfterQuoteIsSent() {

        Booking booking = createValidBooking();

        booking.accept();
        booking.quoteSent();

        booking.cancel();

        assertEquals(
                BookingStatus.CANCELLED,
                booking.getStatus()
        );

        assertNotNull(booking.getCancelledAt());
    }

    @Test
    void shouldNotCancelCompletedBooking() {

        Booking booking = createValidBooking();

        booking.accept();
        booking.quoteSent();
        booking.quoteAccepted(
                new BigDecimal("175.00")
        );
        booking.paymentCompleted();
        booking.materialsReleased();
        booking.start();
        booking.complete();

        assertThrows(
                InvalidBookingStateException.class,
                booking::cancel
        );
    }

    // ---------------------------------------------------------
    // Update
    // ---------------------------------------------------------

    @Test
    void shouldUpdateRequestedBooking() {

        Booking booking = createValidBooking();

        String newDescription =
                "Updated plumbing repair";

        String newNotes =
                "Customer will be home after 10 AM";

        BookingAddress newAddress =
                createAddress();

        LocalDateTime newStart =
                LocalDateTime.now().plusDays(5);

        LocalDateTime newEnd =
                newStart.plusHours(3);

        booking.update(
                newDescription,
                newNotes,
                newAddress,
                newStart,
                newEnd
        );

        assertEquals(
                newDescription,
                booking.getDescription()
        );

        assertEquals(
                newNotes,
                booking.getNotes()
        );

        assertEquals(
                newAddress,
                booking.getAddress()
        );

        assertEquals(
                newStart,
                booking.getScheduledStart()
        );

        assertEquals(
                newEnd,
                booking.getScheduledEnd()
        );
    }

    @Test
    void shouldNotUpdateBookingAfterQuoteIsSent() {

        Booking booking = createValidBooking();

        booking.accept();
        booking.quoteSent();

        assertThrows(
                InvalidBookingStateException.class,
                () -> booking.update(
                        "Updated description",
                        "Updated notes",
                        address,
                        start,
                        end
                )
        );
    }

    @Test
    void shouldRejectInvalidScheduleDuringUpdate() {

        Booking booking = createValidBooking();

        assertThrows(
                IllegalArgumentException.class,
                () -> booking.update(
                        DESCRIPTION,
                        NOTES,
                        address,
                        end,
                        start
                )
        );
    }

    // ---------------------------------------------------------
    // Test helpers
    // ---------------------------------------------------------

    private Booking createValidBooking() {

        return Booking.create(
                USER_ID,
                PROVIDER_USER_ID,
                PROVIDER_SERVICE_ID,
                SERVICE_NAME,
                PRICE,
                DESCRIPTION,
                NOTES,
                address,
                start,
                end
        );
    }

    private BookingAddress createAddress() {

        return new BookingAddress(
                "12 Test Street",
                null,
                "Maiduguri",
                "Borno",
                "600001",
                "Nigeria"
        );
    }
}