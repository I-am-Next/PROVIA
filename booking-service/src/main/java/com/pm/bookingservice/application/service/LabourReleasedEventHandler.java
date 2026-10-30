package com.pm.bookingservice.application.service;

import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.BookingStatus;
import com.pm.bookingservice.domain.model.BookingStatusHistory;
import com.pm.bookingservice.domain.model.quote.BookingQuote;
import com.pm.bookingservice.domain.model.quote.QuoteStatus;
import com.pm.bookingservice.domain.repository.BookingRepository;
import com.pm.bookingservice.domain.repository.BookingStatusHistoryRepository;
import com.pm.bookingservice.domain.repository.quote.BookingQuoteRepository;
import com.pm.bookingservice.infrastructure.inbox.ProcessedEvent;
import com.pm.bookingservice.infrastructure.inbox.ProcessedEventRepository;
import com.pm.contracts.escrow.LabourReleasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class LabourReleasedEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(LabourReleasedEventHandler.class);

    private final BookingRepository bookingRepository;
    private final BookingQuoteRepository bookingQuoteRepository;
    private final BookingStatusHistoryRepository statusHistoryRepository;
    private final ProcessedEventRepository processedEventRepository;

    public LabourReleasedEventHandler(
            BookingRepository bookingRepository,
            BookingQuoteRepository bookingQuoteRepository,
            BookingStatusHistoryRepository statusHistoryRepository,
            ProcessedEventRepository processedEventRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingQuoteRepository = bookingQuoteRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void handle(LabourReleasedEvent event) {
        validateEvent(event);

        if (processedEventRepository.existsById(event.eventId())) {
            log.info(
                    "Ignoring duplicate LABOUR_RELEASED event. eventId={}",
                    event.eventId()
            );
            return;
        }

        Booking booking = bookingRepository.findById(event.bookingId())
                .orElseThrow(() -> new IllegalStateException(
                        "Booking not found for labour release: "
                                + event.bookingId()
                ));

        if (!Objects.equals(booking.getUserId(), event.customerId())) {
            throw new IllegalStateException(
                    "Labour release customer does not match booking customer."
            );
        }

        if (!Objects.equals(
                booking.getProviderUserId(),
                event.providerId()
        )) {
            throw new IllegalStateException(
                    "Labour release provider does not match booking provider."
            );
        }

        BookingQuote quote = bookingQuoteRepository
                .findTopByBookingIdAndStatusOrderByCreatedAtDesc(
                        event.bookingId(),
                        QuoteStatus.ACCEPTED
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Accepted quote not found for labour release booking: "
                                + event.bookingId()
                ));

        if (!Objects.equals(quote.getProviderId(), event.providerId())) {
            throw new IllegalStateException(
                    "Labour release provider does not match quote provider."
            );
        }

        if (!quote.isMaterialsReleased()) {
            throw new IllegalStateException(
                    "Cannot confirm labour release before materials release."
            );
        }

        /*
         * Payment Service releases the escrow's remaining balance.
         * After materials release, that balance should equal the
         * agreed labour amount plus the service fee.
         */
        BigDecimal expectedLabourRelease =
                quote.getLabourAmount().add(quote.getServiceFee());

        if (expectedLabourRelease.compareTo(event.amount()) != 0) {
            throw new IllegalStateException(
                    "Labour release amount does not match the accepted quote. "
                            + "Expected: " + expectedLabourRelease
                            + ", received: " + event.amount()
            );
        }

        BookingStatus currentStatus = booking.getStatus();

        /*
         * A distinct event ID can arrive after another valid event or
         * recovery process has already completed the booking transition.
         * Do not create duplicate history entries.
         */
        if (currentStatus == BookingStatus.COMPLETION_CONFIRMED) {
            processedEventRepository.save(
                    new ProcessedEvent(
                            event.eventId(),
                            event.eventType()
                    )
            );

            log.info(
                    "Booking {} is already COMPLETION_CONFIRMED; "
                            + "recorded labour release event {} without "
                            + "another transition.",
                    booking.getId(),
                    event.eventId()
            );
            return;
        }

        if (currentStatus != BookingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot apply LABOUR_RELEASED to booking "
                            + booking.getId()
                            + " in state "
                            + currentStatus
            );
        }

        booking.confirmCompletion();
        bookingRepository.save(booking);

        statusHistoryRepository.save(
                BookingStatusHistory.create(
                        booking.getId(),
                        booking.getStatus(),
                        event.customerId(),
                        "Labour release confirmed by Payment Service"
                )
        );

        processedEventRepository.save(
                new ProcessedEvent(
                        event.eventId(),
                        event.eventType()
                )
        );

        log.info(
                "Applied LABOUR_RELEASED. bookingId={}, previousStatus={}, "
                        + "newStatus={}, eventId={}, escrowId={}, paymentId={}",
                booking.getId(),
                currentStatus,
                booking.getStatus(),
                event.eventId(),
                event.escrowId(),
                event.paymentId()
        );
    }

    private void validateEvent(LabourReleasedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "LabourReleasedEvent must not be null."
            );
        }

        if (event.eventId() == null
                || event.escrowId() == null
                || event.paymentId() == null
                || event.bookingId() == null
                || event.customerId() == null
                || event.providerId() == null
                || event.amount() == null
                || event.occurredAt() == null) {
            throw new IllegalArgumentException(
                    "LabourReleasedEvent is missing required fields."
            );
        }

        if (event.eventVersion() != 1) {
            throw new IllegalArgumentException(
                    "Unsupported LABOUR_RELEASED event version: "
                            + event.eventVersion()
            );
        }

        if (event.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Labour release amount must be positive."
            );
        }
    }
}
