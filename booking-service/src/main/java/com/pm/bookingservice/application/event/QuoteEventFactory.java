package com.pm.bookingservice.application.event;

import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.quote.BookingQuote;
import com.pm.contracts.booking.QuoteAcceptedEvent;
import com.pm.contracts.booking.QuoteCreatedEvent;
import com.pm.contracts.booking.QuoteReceivedEvent;
import com.pm.contracts.booking.QuoteRejectedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class QuoteEventFactory {

    public QuoteCreatedEvent quoteCreated(
            Booking booking,
            BookingQuote quote
    ) {

        return new QuoteCreatedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                quote.getId(),

                booking.getId(),

                booking.getProviderUserId()

        );

    }

    public QuoteAcceptedEvent quoteAccepted(
            Booking booking,
            BookingQuote quote
    ) {

        return new QuoteAcceptedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                quote.getId(),

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId()

        );

    }

    public QuoteRejectedEvent quoteRejected(
            Booking booking,
            BookingQuote quote
    ) {

        return new QuoteRejectedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                quote.getId(),

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId()

        );

    }

    public QuoteReceivedEvent quoteReceived(
            Booking booking,
            BookingQuote quote
    ) {

        return new QuoteReceivedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                quote.getId(),

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId()

        );
    }
}