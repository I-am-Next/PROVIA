package com.pm.bookingservice.application.service;

import com.pm.bookingservice.application.dto.request.quote.CreateQuoteRequest;
import com.pm.bookingservice.application.dto.response.quote.BookingQuoteResponse;
import com.pm.bookingservice.application.event.BookingEventFactory;
import com.pm.bookingservice.application.event.DomainEventPublisher;
import com.pm.bookingservice.application.event.QuoteEventFactory;
import com.pm.bookingservice.application.mapper.BookingQuoteMapper;
import com.pm.bookingservice.domain.exception.BookingNotFoundException;
import com.pm.bookingservice.domain.exception.DuplicateQuoteException;
import com.pm.bookingservice.domain.exception.QuoteAccessDeniedException;
import com.pm.bookingservice.domain.exception.QuoteNotFoundException;
import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.BookingStatus;
import com.pm.bookingservice.domain.model.BookingStatusHistory;
import com.pm.bookingservice.domain.model.quote.BookingQuote;
import com.pm.bookingservice.domain.model.quote.QuoteStatus;
import org.springframework.beans.factory.annotation.Value;
import com.pm.bookingservice.domain.repository.BookingRepository;
import com.pm.bookingservice.domain.repository.BookingStatusHistoryRepository;
import com.pm.bookingservice.domain.repository.quote.BookingQuoteRepository;
import com.pm.bookingservice.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class QuoteServiceImpl implements QuoteService {


    private final BookingRepository bookingRepository;

    private final BookingQuoteRepository quoteRepository;

    private final SecurityService securityService;

    private final DomainEventPublisher publisher;

    private final QuoteEventFactory quoteEventFactory;

    private final BookingEventFactory bookingEventFactory;

    private final BookingStatusHistoryRepository statusHistoryRepository;

    private static final Logger log =
            LoggerFactory.getLogger(QuoteServiceImpl.class);

    @Value("${booking.quote.expiration-hours:48}")
    private long quoteExpirationHours;

    public QuoteServiceImpl(

            BookingRepository bookingRepository,

            BookingQuoteRepository quoteRepository,

            SecurityService securityService,

            DomainEventPublisher publisher,

            QuoteEventFactory quoteEventFactory,

            BookingEventFactory bookingEventFactory,

            BookingStatusHistoryRepository statusHistoryRepository

    ) {

        this.bookingRepository = bookingRepository;

        this.quoteRepository = quoteRepository;

        this.securityService = securityService;

        this.publisher = publisher;

        this.quoteEventFactory = quoteEventFactory;

        this.bookingEventFactory = bookingEventFactory;

        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Override
    public BookingQuoteResponse createQuote(
            UUID bookingId,
            CreateQuoteRequest request
    ) {

        UUID providerId =
                securityService.getAuthenticatedUserId();

        Booking booking =
                getBookingOrThrow(
                        bookingId
                );

        validateProviderOwnership(
                booking,
                providerId
        );

        validateActiveQuote(
                bookingId
        );

        BookingQuote quote =
                getBookingQuote(
                        bookingId,
                        request,
                        providerId
                );

        BookingStatus previousStatus =
                booking.getStatus();

        quoteRepository.save(
                quote
        );

        booking.quoteSent();

        bookingRepository.save(
                booking
        );

        /*
         * Record Booking Status History
         */
        recordStatusChange(
                booking,
                providerId,
                "Provider submitted a quote"
        );

        /*
         * Quote Business Event
         */
        publisher.publish(
                quoteEventFactory.quoteReceived(
                        booking,
                        quote
                )
        );

        /*
         * Booking Lifecycle Event
         */
        publisher.publish(
                bookingEventFactory.statusChanged(
                        booking,
                        previousStatus
                )
        );

        return BookingQuoteMapper.toResponse(
                quote
        );
    }

    private BookingQuote getBookingQuote(
            UUID bookingId,
            CreateQuoteRequest request,
            UUID providerId
    ) {

        BigDecimal labourAmount =
                request.labourAmount();

        BigDecimal materialsAmount =
                request.materialsAmount();

        /*
         * Service fee is currently zero.
         *
         * This remains application-level pricing policy.
         * When the platform introduces platform fees,
         * this is the place where that policy can evolve.
         */
        BigDecimal serviceFee =
                BigDecimal.ZERO;

        BigDecimal totalAmount =
                labourAmount
                        .add(materialsAmount)
                        .add(serviceFee);

        Instant expiresAt =
                Instant.now()
                        .plus(
                                quoteExpirationHours,
                                java.time.temporal.ChronoUnit.HOURS
                        );

        return BookingQuote.create(
                bookingId,
                providerId,
                labourAmount,
                materialsAmount,
                serviceFee,
                totalAmount,
                request.description(),
                request.estimatedDurationDays(),
                expiresAt
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BookingQuoteResponse getQuote(
            UUID bookingId
    ) {
        Booking booking =
                getBookingOrThrow(
                        bookingId
                );
        UUID currentUser =
                securityService.getAuthenticatedUserId();

        if (!booking.getUserId().equals(currentUser)
                &&
                !booking.getProviderUserId().equals(currentUser)) {

            throw new QuoteAccessDeniedException(
                    "Access denied"
            );

        }
        BookingQuote quote =
                getQuoteOrThrow(
                        bookingId
                );
        return BookingQuoteMapper.toResponse(
                quote
        );
    }

    @Override
    public BookingQuoteResponse acceptQuote(UUID bookingId) {

        log.info("========== ACCEPT QUOTE ==========");
        log.info("Booking ID={}", bookingId);

        UUID customerId =
                securityService.getAuthenticatedUserId();

        log.info("Authenticated Customer={}", customerId);

        Booking booking =
                getBookingOrThrow(bookingId);

        log.info("Booking loaded:");
        log.info("Booking Status={}", booking.getStatus());
        log.info("Customer={}", booking.getUserId());
        log.info("Provider={}", booking.getProviderUserId());
        log.info("Current Price={}", booking.getPrice());

        validateCustomerOwnership(
                booking,
                customerId
        );

        BookingQuote quote =
                getQuoteOrThrow(bookingId);

        log.info("Quote loaded:");
        log.info("Quote Status={}", quote.getStatus());
        log.info("Quote Total={}", quote.getTotalAmount());

        BookingStatus previousStatus =
                booking.getStatus();

        log.info("Accepting quote...");

        quote.accept();

        booking.quoteAccepted(
                quote.getTotalAmount()
        );

        quoteRepository.save(quote);

        bookingRepository.save(booking);

        /*
         * Record Booking Status History
         */
        recordStatusChange(
                booking,
                customerId,
                "Customer accepted provider quote"
        );

        /*
         * Quote Business Event
         */
        publisher.publish(
                quoteEventFactory.quoteAccepted(
                        booking,
                        quote
                )
        );

        /*
         * Booking Business Event
         */
        publisher.publish(
                bookingEventFactory.bookingUpdated(
                        booking
                )
        );

        /*
         * Booking Lifecycle Event
         */
        publisher.publish(
                bookingEventFactory.statusChanged(
                        booking,
                        previousStatus
                )
        );

        log.info("QuoteAcceptedEvent published.");
        log.info("========== ACCEPT QUOTE COMPLETE ==========");

        return BookingQuoteMapper.toResponse(
                quote
        );
    }
    @Override
    public BookingQuoteResponse rejectQuote(
            UUID bookingId
    ) {

        UUID customerId =
                securityService.getAuthenticatedUserId();

        Booking booking =
                getBookingOrThrow(
                        bookingId
                );

        validateCustomerOwnership(
                booking,
                customerId
        );

        BookingQuote quote =
                getQuoteOrThrow(
                        bookingId
                );

        BookingStatus previousStatus =
                booking.getStatus();

        quote.reject();

        booking.quoteRejected();

        quoteRepository.save(
                quote
        );

        bookingRepository.save(
                booking
        );

        /*
         * Record Booking Status History
         */
        recordStatusChange(
                booking,
                customerId,
                "Customer rejected provider quote"
        );

        /*
         * Quote Business Event
         */
        publisher.publish(
                quoteEventFactory.quoteRejected(
                        booking,
                        quote
                )
        );

        /*
         * Booking Lifecycle Event
         */
        publisher.publish(
                bookingEventFactory.statusChanged(
                        booking,
                        previousStatus
                )
        );

        return BookingQuoteMapper.toResponse(
                quote
        );
    }

    /*
     *
     * Helpers
     *
     */

    private void recordStatusChange(
            Booking booking,
            UUID changedBy,
            String note
    ) {

        statusHistoryRepository.save(

                BookingStatusHistory.create(

                        booking.getId(),

                        booking.getStatus(),

                        changedBy,

                        note

                )

        );
    }

    private Booking getBookingOrThrow(UUID bookingId) {

        log.info("Loading Booking {}", bookingId);

        Booking booking = bookingRepository.findById(
                bookingId
        ).orElseThrow(() -> new BookingNotFoundException(
                bookingId
        ));

        log.info("Loaded Booking Status={}", booking.getStatus());

        return booking;
    }


    private BookingQuote getQuoteOrThrow(UUID bookingId) {

        log.info("Loading latest Quote for booking {}", bookingId);

        BookingQuote quote =
                quoteRepository
                        .findTopByBookingIdOrderByCreatedAtDesc(bookingId)
                        .orElseThrow(() ->
                                new QuoteNotFoundException(bookingId)
                        );

        log.info(
                "Loaded Quote id={} status={}",
                quote.getId(),
                quote.getStatus()
        );

        return quote;
    }



    private void validateProviderOwnership(

            Booking booking,

            UUID providerId

    ) {

        if (!booking.getProviderUserId().equals(providerId)) {

            throw new QuoteAccessDeniedException(

                    "Booking does not belong to provider"

            );
        }

    }



    private void validateCustomerOwnership(

            Booking booking,

            UUID customerId

    ) {

        if (!booking.getUserId().equals(customerId)) {

            throw new QuoteAccessDeniedException(

                    "Booking does not belong to customer"

            );
        }

    }



    private void validateActiveQuote(

            UUID bookingId

    ) {


        boolean exists =

                quoteRepository.existsByBookingIdAndStatus(

                        bookingId,

                        QuoteStatus.PENDING

                );


        if (exists) {

            throw new DuplicateQuoteException();

        }


    }

}
