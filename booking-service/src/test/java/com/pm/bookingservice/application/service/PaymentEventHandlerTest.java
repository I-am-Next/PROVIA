package com.pm.bookingservice.application.service;

import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.BookingAddress;
import com.pm.bookingservice.domain.model.BookingStatus;
import com.pm.bookingservice.domain.model.BookingStatusHistory;
import com.pm.bookingservice.domain.repository.BookingRepository;
import com.pm.bookingservice.domain.repository.BookingStatusHistoryRepository;
import com.pm.bookingservice.infrastructure.inbox.ProcessedEvent;
import com.pm.bookingservice.infrastructure.inbox.ProcessedEventRepository;
import com.pm.contracts.payment.PaymentCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventHandlerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingStatusHistoryRepository bookingStatusHistoryRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private PaymentEventHandler paymentEventHandler;

    private UUID bookingId;
    private UUID customerId;
    private UUID providerId;
    private UUID paymentId;
    private UUID escrowId;
    private UUID eventId;

    @BeforeEach
    void setUp() {

        paymentEventHandler = new PaymentEventHandler(
                bookingRepository,
                bookingStatusHistoryRepository,
                processedEventRepository
        );

        bookingId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        escrowId = UUID.randomUUID();
        eventId = UUID.randomUUID();
    }

    @Test
    void shouldMoveBookingFromQuoteAcceptedToPaymentCompleted() {

        Booking booking = createQuoteAcceptedBooking();

        when(processedEventRepository.existsById(eventId))
                .thenReturn(false);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        PaymentCompletedEvent event =
                createPaymentCompletedEvent();

        paymentEventHandler.handle(event);

        assertEquals(
                BookingStatus.PAYMENT_COMPLETED,
                booking.getStatus()
        );

        verify(bookingRepository)
                .findById(bookingId);

        verify(bookingRepository)
                .save(booking);

        verify(bookingStatusHistoryRepository)
                .save(any(BookingStatusHistory.class));

        verify(processedEventRepository)
                .save(any(ProcessedEvent.class));
    }

    @Test
    void shouldRecordPaymentCompletedStatusHistory() {

        Booking booking = createQuoteAcceptedBooking();

        when(processedEventRepository.existsById(eventId))
                .thenReturn(false);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        PaymentCompletedEvent event =
                createPaymentCompletedEvent();

        paymentEventHandler.handle(event);

        ArgumentCaptor<BookingStatusHistory> captor =
                ArgumentCaptor.forClass(
                        BookingStatusHistory.class
                );

        verify(bookingStatusHistoryRepository)
                .save(captor.capture());

        BookingStatusHistory history =
                captor.getValue();

        assertEquals(
                bookingId,
                history.getBookingId()
        );

        assertEquals(
                BookingStatus.PAYMENT_COMPLETED,
                history.getStatus()
        );

        assertEquals(
                customerId,
                history.getChangedBy()
        );

        assertEquals(
                "Payment completed successfully",
                history.getNote()
        );
    }

    @Test
    void shouldIgnoreDuplicatePaymentCompletedEvent() {

        when(processedEventRepository.existsById(eventId))
                .thenReturn(true);

        PaymentCompletedEvent event =
                createPaymentCompletedEvent();

        paymentEventHandler.handle(event);

        verify(processedEventRepository)
                .existsById(eventId);

        verify(bookingRepository, never())
                .findById(any());

        verify(bookingRepository, never())
                .save(any());

        verify(bookingStatusHistoryRepository, never())
                .save(any());

        verify(processedEventRepository, never())
                .save(any());
    }

    @Test
    void shouldFailWhenBookingDoesNotExist() {

        when(processedEventRepository.existsById(eventId))
                .thenReturn(false);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        PaymentCompletedEvent event =
                createPaymentCompletedEvent();

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> paymentEventHandler.handle(event)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Booking not found")
        );

        verify(bookingRepository)
                .findById(bookingId);

        verify(bookingRepository, never())
                .save(any());

        verify(bookingStatusHistoryRepository, never())
                .save(any());

        verify(processedEventRepository, never())
                .save(any());
    }

    private Booking createQuoteAcceptedBooking() {

        Booking booking = Booking.create(
                customerId,
                providerId,
                UUID.randomUUID(),
                "Plumbing Service",
                new BigDecimal("100.00"),
                "Fix leaking pipe",
                null,
                createAddress(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now()
                        .plusDays(1)
                        .plusHours(2)
        );

        /*
         * Booking.create() starts at REQUESTED.
         *
         * Move it through the real domain lifecycle:
         *
         * REQUESTED
         *     ↓
         * AWAITING_QUOTE
         *     ↓
         * QUOTE_SENT
         *     ↓
         * QUOTE_ACCEPTED
         */
        booking.accept();
        booking.quoteSent();
        booking.quoteAccepted(
                new BigDecimal("100.00")
        );

        /*
         * JPA normally supplies this ID.
         * The unit test is not using JPA, so assign it manually.
         */
        ReflectionTestUtils.setField(
                booking,
                "id",
                bookingId
        );

        return booking;
    }

    private PaymentCompletedEvent createPaymentCompletedEvent() {

        return new PaymentCompletedEvent(
                eventId,
                1,
                Instant.now(),
                paymentId,
                bookingId,
                escrowId,
                customerId,
                providerId,
                new BigDecimal("100.00"),
                "NGN",
                "PAYSTACK-REF-123",
                "SUCCESS",
                Instant.now()
        );
    }

    private BookingAddress createAddress() {

        return new BookingAddress(
                "12 Test Street",
                null,
                "Lagos",
                "Lagos",
                "100001",
                "Nigeria"
        );
    }
}