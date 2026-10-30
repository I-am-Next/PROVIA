package com.pm.bookingservice.domain.repository.quote;

import com.pm.bookingservice.domain.model.quote.BookingQuote;
import com.pm.bookingservice.domain.model.quote.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingQuoteRepository
        extends JpaRepository<BookingQuote, UUID> {

    Optional<BookingQuote> findTopByBookingIdOrderByCreatedAtDesc(
            UUID bookingId
    );

    Optional<BookingQuote> findTopByBookingIdAndStatusOrderByCreatedAtDesc(
            UUID bookingId,
            QuoteStatus status
    );

    boolean existsByBookingIdAndStatus(
            UUID bookingId,
            QuoteStatus status
    );
}
