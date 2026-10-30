package com.pm.bookingservice.application.service;

import com.pm.bookingservice.application.dto.request.quote.CreateQuoteRequest;
import com.pm.bookingservice.application.dto.response.quote.BookingQuoteResponse;
import com.pm.bookingservice.application.event.BookingEventFactory;
import com.pm.bookingservice.application.event.DomainEventPublisher;
import com.pm.bookingservice.application.event.QuoteEventFactory;
import com.pm.bookingservice.domain.exception.DuplicateQuoteException;
import com.pm.bookingservice.domain.exception.QuoteAccessDeniedException;
import com.pm.bookingservice.domain.exception.QuoteNotFoundException;
import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.BookingAddress;
import com.pm.bookingservice.domain.model.quote.BookingQuote;
import com.pm.bookingservice.domain.model.quote.QuoteStatus;
import com.pm.bookingservice.domain.repository.BookingRepository;
import com.pm.bookingservice.domain.repository.BookingStatusHistoryRepository;
import com.pm.bookingservice.domain.repository.quote.BookingQuoteRepository;
import com.pm.bookingservice.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class QuoteServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingQuoteRepository quoteRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private DomainEventPublisher publisher;

    @Mock
    private QuoteEventFactory quoteEventFactory;

    @Mock
    private BookingEventFactory bookingEventFactory;

    @Mock
    private BookingStatusHistoryRepository statusHistoryRepository;

    private QuoteServiceImpl quoteService;

    private UUID bookingId;
    private UUID customerId;
    private UUID providerId;

    @BeforeEach
    void setUp() {

        quoteService = new QuoteServiceImpl(
                bookingRepository,
                quoteRepository,
                securityService,
                publisher,
                quoteEventFactory,
                bookingEventFactory,
                statusHistoryRepository
        );

        /*
         * QuoteServiceImpl normally receives this value from Spring
         * through @Value("${booking.quote.expiration-hours:48}").
         *
         * This is a unit test, so Spring does not perform field injection.
         */
        ReflectionTestUtils.setField(
                quoteService,
                "quoteExpirationHours",
                48L
        );

        bookingId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        providerId = UUID.randomUUID();
    }

    @Test
    void shouldReturnLatestQuoteForBooking() {

        Booking booking = createAwaitingQuoteBooking();

        BookingQuote quote = createQuote();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(quoteRepository.findTopByBookingIdOrderByCreatedAtDesc(bookingId))
                .thenReturn(Optional.of(quote));

        BookingQuoteResponse response =
                quoteService.getQuote(bookingId);

        assertNotNull(response);

        assertEquals(
                bookingId,
                response.bookingId()
        );

        assertEquals(
                providerId,
                response.providerId()
        );

        verify(bookingRepository)
                .findById(bookingId);

        verify(quoteRepository)
                .findTopByBookingIdOrderByCreatedAtDesc(bookingId);

        verifyNoMoreInteractions(quoteRepository);
    }

    @Test
    void shouldThrowWhenQuoteDoesNotExist() {

        Booking booking = createAwaitingQuoteBooking();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(quoteRepository.findTopByBookingIdOrderByCreatedAtDesc(bookingId))
                .thenReturn(Optional.empty());

        assertThrows(
                QuoteNotFoundException.class,
                () -> quoteService.getQuote(bookingId)
        );

        verify(quoteRepository)
                .findTopByBookingIdOrderByCreatedAtDesc(bookingId);
    }

    @Test
    void shouldRejectQuoteAccessForUnrelatedUser() {

        Booking booking = createAwaitingQuoteBooking();

        UUID unrelatedUserId = UUID.randomUUID();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(securityService.getAuthenticatedUserId())
                .thenReturn(unrelatedUserId);

        assertThrows(
                QuoteAccessDeniedException.class,
                () -> quoteService.getQuote(bookingId)
        );

        verify(
                quoteRepository,
                never()
        ).findTopByBookingIdOrderByCreatedAtDesc(bookingId);
    }

    @Test
    void shouldRejectCreatingSecondPendingQuote() {

        Booking booking = createAwaitingQuoteBooking();

        CreateQuoteRequest request = createQuoteRequest();

        when(securityService.getAuthenticatedUserId())
                .thenReturn(providerId);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(quoteRepository.existsByBookingIdAndStatus(
                bookingId,
                QuoteStatus.PENDING
        )).thenReturn(true);

        assertThrows(
                DuplicateQuoteException.class,
                () -> quoteService.createQuote(
                        bookingId,
                        request
                )
        );

        verify(
                quoteRepository
        ).existsByBookingIdAndStatus(
                bookingId,
                QuoteStatus.PENDING
        );

        verify(
                quoteRepository,
                never()
        ).save(any(BookingQuote.class));

        verify(
                bookingRepository,
                never()
        ).save(any(Booking.class));
    }

    @Test
    void shouldCreateQuoteWhenNoPendingQuoteExists() {

        Booking booking = createAwaitingQuoteBooking();

        CreateQuoteRequest request = createQuoteRequest();

        when(securityService.getAuthenticatedUserId())
                .thenReturn(providerId);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(quoteRepository.existsByBookingIdAndStatus(
                bookingId,
                QuoteStatus.PENDING
        )).thenReturn(false);

        when(quoteRepository.save(any(BookingQuote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingQuoteResponse response =
                quoteService.createQuote(
                        bookingId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                bookingId,
                response.bookingId()
        );

        assertEquals(
                providerId,
                response.providerId()
        );

        assertEquals(
                new BigDecimal("150.00"),
                response.totalAmount()
        );

        assertEquals(
                QuoteStatus.PENDING,
                response.status()
        );

        assertNotNull(
                response.expiresAt()
        );

        assertTrue(
                response.expiresAt().isAfter(Instant.now())
        );

        assertEquals(
                com.pm.bookingservice.domain.model.BookingStatus.QUOTE_SENT,
                booking.getStatus()
        );

        verify(
                quoteRepository
        ).existsByBookingIdAndStatus(
                bookingId,
                QuoteStatus.PENDING
        );

        verify(
                quoteRepository
        ).save(any(BookingQuote.class));

        verify(
                bookingRepository
        ).save(booking);

        verify(
                statusHistoryRepository
        ).save(any());

        verify(
                publisher,
                times(2)
        ).publish(any());
    }

    @Test
    void shouldRejectQuoteCreationForAnotherProvider() {

        Booking booking = createAwaitingQuoteBooking();

        UUID anotherProviderId = UUID.randomUUID();

        when(securityService.getAuthenticatedUserId())
                .thenReturn(anotherProviderId);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        assertThrows(
                QuoteAccessDeniedException.class,
                () -> quoteService.createQuote(
                        bookingId,
                        createQuoteRequest()
                )
        );

        verify(
                quoteRepository,
                never()
        ).existsByBookingIdAndStatus(
                any(),
                any()
        );

        verify(
                quoteRepository,
                never()
        ).save(any(BookingQuote.class));
    }

    private Booking createAwaitingQuoteBooking() {

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
                LocalDateTime.now().plusDays(1).plusHours(2)
        );

        /*
         * Booking.create() starts at REQUESTED.
         * A provider can only submit a quote when the
         * booking has reached AWAITING_QUOTE.
         */
        booking.accept();

        assertEquals(
                com.pm.bookingservice.domain.model.BookingStatus.AWAITING_QUOTE,
                booking.getStatus()
        );

        return booking;
    }

    private CreateQuoteRequest createQuoteRequest() {

        return new CreateQuoteRequest(
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                "Pipe repair and replacement",
                2
        );
    }

    private BookingQuote createQuote() {

        return BookingQuote.create(
                bookingId,
                providerId,
                new BigDecimal("80.00"),
                new BigDecimal("20.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                "Pipe repair",
                2,
                Instant.now().plusSeconds(3600)
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