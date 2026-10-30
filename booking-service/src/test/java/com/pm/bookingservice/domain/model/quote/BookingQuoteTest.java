package com.pm.bookingservice.domain.model.quote;

import com.pm.bookingservice.domain.exception.InvalidQuoteStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingQuoteTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();
    private static final UUID PROVIDER_ID = UUID.randomUUID();

    private static final BigDecimal LABOUR_AMOUNT =
            new BigDecimal("100.00");

    private static final BigDecimal MATERIALS_AMOUNT =
            new BigDecimal("50.00");

    private static final BigDecimal SERVICE_FEE =
            new BigDecimal("15.00");

    private static final BigDecimal TOTAL_AMOUNT =
            new BigDecimal("165.00");

    private static final String DESCRIPTION =
            "Repair leaking kitchen pipe";

    private static final Integer ESTIMATED_DURATION_DAYS = 3;

    private final Instant futureExpiration =
            Instant.now().plusSeconds(3600);

    // ---------------------------------------------------------
    // Creation
    // ---------------------------------------------------------

    @Test
    void shouldCreateQuoteWithValidData() {

        BookingQuote quote = createValidQuote();

        assertNotNull(quote);

        assertEquals(BOOKING_ID, quote.getBookingId());
        assertEquals(PROVIDER_ID, quote.getProviderId());

        assertEquals(
                LABOUR_AMOUNT,
                quote.getLabourAmount()
        );

        assertEquals(
                MATERIALS_AMOUNT,
                quote.getMaterialsAmount()
        );

        assertEquals(
                SERVICE_FEE,
                quote.getServiceFee()
        );

        assertEquals(
                TOTAL_AMOUNT,
                quote.getTotalAmount()
        );

        assertEquals(
                DESCRIPTION,
                quote.getDescription()
        );

        assertEquals(
                ESTIMATED_DURATION_DAYS,
                quote.getEstimatedDurationDays()
        );

        assertEquals(
                futureExpiration,
                quote.getExpiresAt()
        );

        assertEquals(
                QuoteStatus.PENDING,
                quote.getStatus()
        );

        assertFalse(quote.isMaterialsReleased());
    }

    @Test
    void shouldRejectNullBookingId() {

        assertThrows(
                NullPointerException.class,
                () -> BookingQuote.create(
                        null,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectNullProviderId() {

        assertThrows(
                NullPointerException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        null,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    // ---------------------------------------------------------
    // Monetary validation
    // ---------------------------------------------------------

    @Test
    void shouldRejectNullLabourAmount() {

        assertThrows(
                NullPointerException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        null,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectNegativeLabourAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        new BigDecimal("-1.00"),
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectNullMaterialsAmount() {

        assertThrows(
                NullPointerException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        null,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectNegativeMaterialsAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        new BigDecimal("-1.00"),
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectNullServiceFee() {

        assertThrows(
                NullPointerException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        null,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectNegativeServiceFee() {

        assertThrows(
                IllegalArgumentException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        new BigDecimal("-1.00"),
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldAllowZeroMonetaryAmounts() {

        BookingQuote quote = BookingQuote.create(
                BOOKING_ID,
                PROVIDER_ID,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                DESCRIPTION,
                ESTIMATED_DURATION_DAYS,
                futureExpiration
        );

        assertEquals(
                BigDecimal.ZERO,
                quote.getTotalAmount()
        );
    }

    // ---------------------------------------------------------
    // Total amount validation
    // ---------------------------------------------------------

    @Test
    void shouldCalculateCorrectTotalAmount() {

        BookingQuote quote = BookingQuote.create(
                BOOKING_ID,
                PROVIDER_ID,
                new BigDecimal("100.00"),
                new BigDecimal("75.00"),
                new BigDecimal("25.00"),
                new BigDecimal("200.00"),
                DESCRIPTION,
                ESTIMATED_DURATION_DAYS,
                futureExpiration
        );

        assertEquals(
                new BigDecimal("200.00"),
                quote.getTotalAmount()
        );
    }

    @Test
    void shouldRejectIncorrectTotalAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        new BigDecimal("200.00"),
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectNullTotalAmount() {

        assertThrows(
                NullPointerException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        null,
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldCompareTotalAmountByValueRatherThanScale() {

        BookingQuote quote = BookingQuote.create(
                BOOKING_ID,
                PROVIDER_ID,
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("15.00"),
                new BigDecimal("165.000"),
                DESCRIPTION,
                ESTIMATED_DURATION_DAYS,
                futureExpiration
        );

        assertEquals(
                0,
                quote.getTotalAmount()
                        .compareTo(new BigDecimal("165.00"))
        );
    }

    // ---------------------------------------------------------
    // Text and duration validation
    // ---------------------------------------------------------

    @Test
    void shouldRejectBlankDescription() {

        assertThrows(
                IllegalArgumentException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        "   ",
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectNullDescription() {

        assertThrows(
                IllegalArgumentException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        null,
                        ESTIMATED_DURATION_DAYS,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectNullEstimatedDuration() {

        assertThrows(
                IllegalArgumentException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        null,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectZeroEstimatedDuration() {

        assertThrows(
                IllegalArgumentException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        0,
                        futureExpiration
                )
        );
    }

    @Test
    void shouldRejectNegativeEstimatedDuration() {

        assertThrows(
                IllegalArgumentException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        -1,
                        futureExpiration
                )
        );
    }

    // ---------------------------------------------------------
    // Expiration
    // ---------------------------------------------------------

    @Test
    void shouldRejectNullExpiration() {

        assertThrows(
                NullPointerException.class,
                () -> BookingQuote.create(
                        BOOKING_ID,
                        PROVIDER_ID,
                        LABOUR_AMOUNT,
                        MATERIALS_AMOUNT,
                        SERVICE_FEE,
                        TOTAL_AMOUNT,
                        DESCRIPTION,
                        ESTIMATED_DURATION_DAYS,
                        null
                )
        );
    }

    @Test
    void shouldReportFalseWhenQuoteHasNotExpired() {

        BookingQuote quote = createValidQuote();

        assertFalse(quote.isExpired());
    }

    @Test
    void shouldReportTrueWhenQuoteHasExpired() {

        BookingQuote quote = BookingQuote.create(
                BOOKING_ID,
                PROVIDER_ID,
                LABOUR_AMOUNT,
                MATERIALS_AMOUNT,
                SERVICE_FEE,
                TOTAL_AMOUNT,
                DESCRIPTION,
                ESTIMATED_DURATION_DAYS,
                Instant.now().minusSeconds(60)
        );

        assertTrue(quote.isExpired());
    }

    // ---------------------------------------------------------
    // Quote lifecycle
    // ---------------------------------------------------------

    @Test
    void shouldAcceptPendingQuote() {

        BookingQuote quote = createValidQuote();

        quote.accept();

        assertEquals(
                QuoteStatus.ACCEPTED,
                quote.getStatus()
        );
    }

    @Test
    void shouldRejectPendingQuote() {

        BookingQuote quote = createValidQuote();

        quote.reject();

        assertEquals(
                QuoteStatus.REJECTED,
                quote.getStatus()
        );
    }

    @Test
    void shouldExpirePendingQuote() {

        BookingQuote quote = createValidQuote();

        quote.expire();

        assertEquals(
                QuoteStatus.EXPIRED,
                quote.getStatus()
        );
    }

    // ---------------------------------------------------------
    // Invalid lifecycle transitions
    // ---------------------------------------------------------

    @Test
    void shouldNotAcceptAlreadyAcceptedQuote() {

        BookingQuote quote = createValidQuote();

        quote.accept();

        assertThrows(
                InvalidQuoteStateException.class,
                quote::accept
        );
    }

    @Test
    void shouldNotRejectAlreadyAcceptedQuote() {

        BookingQuote quote = createValidQuote();

        quote.accept();

        assertThrows(
                InvalidQuoteStateException.class,
                quote::reject
        );
    }

    @Test
    void shouldNotExpireAlreadyAcceptedQuote() {

        BookingQuote quote = createValidQuote();

        quote.accept();

        assertThrows(
                InvalidQuoteStateException.class,
                quote::expire
        );
    }

    @Test
    void shouldNotAcceptRejectedQuote() {

        BookingQuote quote = createValidQuote();

        quote.reject();

        assertThrows(
                InvalidQuoteStateException.class,
                quote::accept
        );
    }

    @Test
    void shouldNotAcceptExpiredQuote() {

        BookingQuote quote = createValidQuote();

        quote.expire();

        assertThrows(
                InvalidQuoteStateException.class,
                quote::accept
        );
    }

    // ---------------------------------------------------------
    // Materials release
    // ---------------------------------------------------------

    @Test
    void shouldReleaseMaterialsAfterQuoteIsAccepted() {

        BookingQuote quote = createValidQuote();

        quote.accept();
        quote.markMaterialsReleased();

        assertTrue(quote.isMaterialsReleased());
    }

    @Test
    void shouldNotReleaseMaterialsWhileQuoteIsPending() {

        BookingQuote quote = createValidQuote();

        assertThrows(
                InvalidQuoteStateException.class,
                quote::markMaterialsReleased
        );
    }

    @Test
    void shouldNotReleaseMaterialsAfterQuoteIsRejected() {

        BookingQuote quote = createValidQuote();

        quote.reject();

        assertThrows(
                InvalidQuoteStateException.class,
                quote::markMaterialsReleased
        );
    }

    @Test
    void shouldNotReleaseMaterialsAfterQuoteExpires() {

        BookingQuote quote = createValidQuote();

        quote.expire();

        assertThrows(
                InvalidQuoteStateException.class,
                quote::markMaterialsReleased
        );
    }

    @Test
    void shouldNotReleaseMaterialsTwice() {

        BookingQuote quote = createValidQuote();

        quote.accept();
        quote.markMaterialsReleased();

        assertThrows(
                InvalidQuoteStateException.class,
                quote::markMaterialsReleased
        );
    }

    // ---------------------------------------------------------
    // Test helper
    // ---------------------------------------------------------

    private BookingQuote createValidQuote() {

        return BookingQuote.create(
                BOOKING_ID,
                PROVIDER_ID,
                LABOUR_AMOUNT,
                MATERIALS_AMOUNT,
                SERVICE_FEE,
                TOTAL_AMOUNT,
                DESCRIPTION,
                ESTIMATED_DURATION_DAYS,
                futureExpiration
        );
    }
}
