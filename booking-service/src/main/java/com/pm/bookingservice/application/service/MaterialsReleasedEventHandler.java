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
import com.pm.contracts.escrow.MaterialsReleasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class MaterialsReleasedEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(MaterialsReleasedEventHandler.class);

    private final BookingRepository bookingRepository;
    private final BookingQuoteRepository bookingQuoteRepository;
    private final BookingStatusHistoryRepository statusHistoryRepository;
    private final ProcessedEventRepository processedEventRepository;

    public MaterialsReleasedEventHandler(
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
    public void handle(MaterialsReleasedEvent event) {
        validateEvent(event);

        if (processedEventRepository.existsById(event.eventId())) {
            log.info(
                    "Ignoring duplicate MATERIALS_RELEASED event. eventId={}",
                    event.eventId()
            );
            return;
        }

        Booking booking = bookingRepository.findById(event.bookingId())
                .orElseThrow(() -> new IllegalStateException(
                        "Booking not found: " + event.bookingId()
                ));

        if (!Objects.equals(booking.getUserId(), event.customerId())) {
            throw new IllegalStateException(
                    "Event customer does not match booking."
            );
        }

        if (!Objects.equals(
                booking.getProviderUserId(),
                event.providerId()
        )) {
            throw new IllegalStateException(
                    "Event provider does not match booking."
            );
        }

        BookingQuote quote = bookingQuoteRepository
                .findById(event.quoteId())
                .orElseThrow(() -> new IllegalStateException(
                        "Quote not found: " + event.quoteId()
                ));

        if (!Objects.equals(quote.getBookingId(), event.bookingId())) {
            throw new IllegalStateException(
                    "Event quote does not belong to the event booking."
            );
        }

        if (!Objects.equals(quote.getProviderId(), event.providerId())) {
            throw new IllegalStateException(
                    "Event provider does not match the quote provider."
            );
        }

        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Materials release quote is not accepted."
            );
        }

        if (quote.getMaterialsAmount().compareTo(event.amount()) != 0) {
            throw new IllegalStateException(
                    "Materials release amount does not match the quote."
            );
        }

        if (booking.getStatus().isMaterialsReleased()) {
            if (!quote.isMaterialsReleased()) {
                quote.markMaterialsReleased();
                bookingQuoteRepository.save(quote);
            }

            processedEventRepository.save(
                    new ProcessedEvent(
                            event.eventId(),
                            event.eventType()
                    )
            );
            return;
        }

        if (!booking.getStatus().isPaymentCompleted()) {
            throw new IllegalStateException(
                    "Cannot apply MATERIALS_RELEASED from booking status "
                            + booking.getStatus()
            );
        }

        if (quote.isMaterialsReleased()) {
            throw new IllegalStateException(
                    "Quote is marked released but booking is not. "
                            + "Reconciliation is required."
            );
        }

        BookingStatus previousStatus = booking.getStatus();

        booking.materialsReleased();
        quote.markMaterialsReleased();

        bookingRepository.save(booking);
        bookingQuoteRepository.save(quote);

        statusHistoryRepository.save(
                BookingStatusHistory.create(
                        booking.getId(),
                        booking.getStatus(),
                        event.providerId(),
                        "Materials release confirmed by Payment Service"
                )
        );

        processedEventRepository.save(
                new ProcessedEvent(
                        event.eventId(),
                        event.eventType()
                )
        );

        log.info(
                "Applied MATERIALS_RELEASED. bookingId={}, previousStatus={}, "
                        + "newStatus={}, eventId={}",
                booking.getId(),
                previousStatus,
                booking.getStatus(),
                event.eventId()
        );
    }

    private void validateEvent(MaterialsReleasedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "MaterialsReleasedEvent must not be null."
            );
        }

        if (event.eventId() == null
                || event.bookingId() == null
                || event.quoteId() == null
                || event.escrowId() == null
                || event.paymentId() == null
                || event.customerId() == null
                || event.providerId() == null
                || event.amount() == null
                || event.occurredAt() == null) {
            throw new IllegalArgumentException(
                    "MaterialsReleasedEvent is missing required fields."
            );
        }

        if (event.eventVersion() != 2) {
            throw new IllegalArgumentException(
                    "Unsupported MATERIALS_RELEASED event version: "
                            + event.eventVersion()
            );
        }

        if (event.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Materials release amount must be positive."
            );
        }
    }
}
