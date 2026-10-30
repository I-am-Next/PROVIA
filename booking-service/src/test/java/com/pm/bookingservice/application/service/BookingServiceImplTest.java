package com.pm.bookingservice.application.service;

import com.pm.bookingservice.application.dto.response.CompletionConfirmationResponse;
import com.pm.bookingservice.application.event.BookingEventFactory;
import com.pm.bookingservice.application.event.DomainEventPublisher;
import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.BookingAddress;
import com.pm.bookingservice.domain.model.BookingStatus;
import com.pm.bookingservice.domain.repository.BookingRepository;
import com.pm.bookingservice.domain.repository.BookingStatusHistoryRepository;
import com.pm.bookingservice.domain.repository.quote.BookingQuoteRepository;
import com.pm.bookingservice.infrastructure.client.PaymentServiceClient;
import com.pm.bookingservice.infrastructure.client.ProviderServiceClient;
import com.pm.bookingservice.integration.dto.EscrowResponse;
import com.pm.bookingservice.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository repository;

    @Mock
    private ProviderServiceClient providerClient;

    @Mock
    private SecurityService securityService;

    @Mock
    private DomainEventPublisher publisher;

    @Mock
    private BookingQuoteRepository bookingQuoteRepository;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private BookingEventFactory bookingEventFactory;

    @Mock
    private BookingStatusHistoryRepository statusHistoryRepository;

    private BookingServiceImpl bookingService;

    private UUID bookingId;
    private UUID customerId;
    private UUID providerId;
    private UUID providerServiceId;
    private UUID escrowId;

    private Booking booking;

    @BeforeEach
    void setUp() throws Exception {
        bookingService = new BookingServiceImpl(
                repository,
                providerClient,
                securityService,
                publisher,
                bookingQuoteRepository,
                statusHistoryRepository,
                paymentServiceClient,
                bookingEventFactory
        );

        bookingId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        providerServiceId = UUID.randomUUID();
        escrowId = UUID.randomUUID();

        booking = Booking.create(
                customerId,
                providerId,
                providerServiceId,
                "Plumbing service",
                new BigDecimal("50000.00"),
                "Repair a leaking pipe",
                "Please call before arrival",
                new BookingAddress(
                        "12 Example Street",
                        null,
                        "Abuja",
                        "FCT",
                        null,
                        "Nigeria"
                ),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2)
        );

        setBookingId(booking, bookingId);

        // Build a valid booking lifecycle ending in COMPLETED.
        booking.accept();
        booking.quoteSent();
        booking.quoteAccepted(new BigDecimal("50000.00"));
        booking.paymentCompleted();
        booking.materialsReleased();
        booking.start();
        booking.complete();

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }

    @Test
    void shouldRequestLabourReleaseWithoutChangingBookingState() {
        arrangeAuthenticatedCustomerAndBooking();

        when(paymentServiceClient.releaseLabour(bookingId))
                .thenReturn(fullyReleasedEscrow());

        CompletionConfirmationResponse response =
                bookingService.confirmCompletion(bookingId);

        assertNotNull(response);
        assertNotNull(response.escrow());

        assertEquals(
                BookingStatus.COMPLETED,
                booking.getStatus(),
                "Booking must remain COMPLETED until LABOUR_RELEASED is handled"
        );

        verify(paymentServiceClient).releaseLabour(bookingId);
        verify(repository, never()).save(any(Booking.class));

        verifyNoInteractions(statusHistoryRepository);
        verifyNoInteractions(publisher);
        verifyNoInteractions(bookingEventFactory);
    }

    @Test
    void shouldLeaveBookingCompletedWhenPaymentServiceFails() {
        arrangeAuthenticatedCustomerAndBooking();

        when(paymentServiceClient.releaseLabour(bookingId))
                .thenThrow(new RuntimeException(
                        "Payment Service unavailable"
                ));

        assertThrows(
                RuntimeException.class,
                () -> bookingService.confirmCompletion(bookingId)
        );

        assertEquals(
                BookingStatus.COMPLETED,
                booking.getStatus()
        );

        verify(paymentServiceClient).releaseLabour(bookingId);
        verify(repository, never()).save(any(Booking.class));

        verifyNoInteractions(statusHistoryRepository);
        verifyNoInteractions(publisher);
        verifyNoInteractions(bookingEventFactory);
    }

    @Test
    void shouldRejectInvalidEscrowResponseWithoutChangingBookingState() {
        arrangeAuthenticatedCustomerAndBooking();

        EscrowResponse invalidEscrow = new EscrowResponse(
                escrowId,
                UUID.randomUUID(), // Deliberately wrong booking ID.
                new BigDecimal("50000.00"),
                new BigDecimal("50000.00"),
                BigDecimal.ZERO,
                "FULLY_RELEASED"
        );

        when(paymentServiceClient.releaseLabour(bookingId))
                .thenReturn(invalidEscrow);

        assertThrows(
                IllegalStateException.class,
                () -> bookingService.confirmCompletion(bookingId)
        );

        assertEquals(
                BookingStatus.COMPLETED,
                booking.getStatus()
        );

        verify(paymentServiceClient).releaseLabour(bookingId);
        verify(repository, never()).save(any(Booking.class));

        verifyNoInteractions(statusHistoryRepository);
        verifyNoInteractions(publisher);
        verifyNoInteractions(bookingEventFactory);
    }

    @Test
    void confirmCompletionShouldDeclareNotSupportedPropagation()
            throws NoSuchMethodException {

        Transactional annotation =
                BookingServiceImpl.class
                        .getMethod("confirmCompletion", UUID.class)
                        .getAnnotation(Transactional.class);

        assertNotNull(
                annotation,
                "confirmCompletion must declare method-level transaction propagation"
        );

        assertEquals(
                Propagation.NOT_SUPPORTED,
                annotation.propagation(),
                "confirmCompletion must suspend any existing transaction"
        );
    }

    private void arrangeAuthenticatedCustomerAndBooking() {
        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(repository.findById(bookingId))
                .thenReturn(Optional.of(booking));
    }

    private EscrowResponse fullyReleasedEscrow() {
        return new EscrowResponse(
                escrowId,
                bookingId,
                new BigDecimal("50000.00"),
                new BigDecimal("50000.00"),
                BigDecimal.ZERO,
                "FULLY_RELEASED"
        );
    }

    private void setBookingId(Booking target, UUID id)
            throws ReflectiveOperationException {

        Field field = Booking.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
