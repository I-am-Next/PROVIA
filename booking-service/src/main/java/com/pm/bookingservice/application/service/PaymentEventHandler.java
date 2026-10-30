
package com.pm.bookingservice.application.service;

import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.BookingStatus;
import com.pm.bookingservice.domain.model.BookingStatusHistory;
import com.pm.bookingservice.domain.repository.BookingRepository;
import com.pm.bookingservice.domain.repository.BookingStatusHistoryRepository;
import com.pm.bookingservice.infrastructure.inbox.ProcessedEvent;
import com.pm.bookingservice.infrastructure.inbox.ProcessedEventRepository;
import com.pm.contracts.payment.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class PaymentEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventHandler.class);

    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository
            bookingStatusHistoryRepository;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentEventHandler(
            BookingRepository bookingRepository,
            BookingStatusHistoryRepository bookingStatusHistoryRepository,
            ProcessedEventRepository processedEventRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingStatusHistoryRepository =
                bookingStatusHistoryRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void handle(PaymentCompletedEvent event) {

        Objects.requireNonNull(event, "PaymentCompletedEvent must not be null");
        Objects.requireNonNull(event.eventId(), "Event ID must not be null");
        Objects.requireNonNull(event.bookingId(), "Booking ID must not be null");

        log.info(
                "Handling PAYMENT_COMPLETED: eventId={}, paymentId={}, bookingId={}",
                event.eventId(),
                event.paymentId(),
                event.bookingId()
        );

        /*
         * Inbox idempotency:
         * the processed-event record participates in the same
         * database transaction as the booking and history updates.
         */
        if (processedEventRepository.existsById(event.eventId())) {
            log.info(
                    "Ignoring duplicate payment event: eventId={}",
                    event.eventId()
            );
            return;
        }

        Booking booking = bookingRepository
                .findById(event.bookingId())
                .orElseThrow(() -> new IllegalStateException(
                        "Booking not found for payment event: "
                                + event.bookingId()
                ));

        BookingStatus currentStatus = booking.getStatus();

        /*
         * A distinct event ID may describe a payment whose booking
         * has already reached PAYMENT_COMPLETED. Do not create a
         * duplicate status-history entry.
         *
         * Other states are not silently accepted here. In particular,
         * this handler must never release materials or advance the
         * booking beyond payment completion.
         */
        if (currentStatus.isPaymentCompleted()) {

            log.info(
                    "Booking {} is already PAYMENT_COMPLETED; " +
                            "recording this event without another transition.",
                    booking.getId()
            );

        } else {

            if (!currentStatus.isQuoteAccepted()) {
                throw new IllegalStateException(
                        "Cannot apply PAYMENT_COMPLETED to booking "
                                + booking.getId()
                                + " in state "
                                + currentStatus
                );
            }

            booking.paymentCompleted();

            bookingRepository.save(booking);

            bookingStatusHistoryRepository.save(
                    BookingStatusHistory.create(
                            booking.getId(),
                            booking.getStatus(),
                            event.customerId(),
                            "Payment completed successfully"
                    )
            );

            log.info(
                    "Booking {} transitioned from {} to {}",
                    booking.getId(),
                    currentStatus,
                    booking.getStatus()
            );
        }

        /*
         * Save the inbox record in the same transaction.
         * If the booking or history update fails, this record
         * must roll back as well so Kafka can retry the event.
         */
        processedEventRepository.save(
                new ProcessedEvent(
                        event.eventId(),
                        event.eventType()
                )
        );

        log.info(
                "Recorded payment event as processed: eventId={}",
                event.eventId()
        );
    }
}
