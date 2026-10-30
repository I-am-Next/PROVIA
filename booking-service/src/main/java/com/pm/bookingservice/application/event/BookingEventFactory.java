package com.pm.bookingservice.application.event;

import com.pm.bookingservice.domain.event.BookingStatusChangedEvent;
import com.pm.bookingservice.domain.event.JobCompletionConfirmedEvent;
import com.pm.bookingservice.domain.event.MaterialReleaseRequestedEvent;
import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.BookingStatus;
import com.pm.bookingservice.domain.model.quote.BookingQuote;
import com.pm.contracts.booking.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class BookingEventFactory {

    public BookingCreatedEvent bookingCreated(
            Booking booking
    ) {

        return new BookingCreatedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId(),

                booking.getProviderServiceId()

        );

    }

    public BookingAcceptedEvent bookingAccepted(
            Booking booking
    ) {

        return new BookingAcceptedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getProviderUserId(),

                booking.getUserId()

        );

    }

    public BookingRejectedEvent bookingRejected(
            Booking booking
    ) {

        return new BookingRejectedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getProviderUserId(),

                booking.getUserId()

        );

    }

    public BookingStartedEvent bookingStarted(
            Booking booking
    ) {

        return new BookingStartedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getProviderUserId(),

                booking.getUserId()

        );

    }

    public BookingCompletedEvent bookingCompleted(
            Booking booking
    ) {

        return new BookingCompletedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId(),

                booking.getProviderServiceId(),

                booking.getServiceName()

        );

    }

    public BookingCancelledEvent bookingCancelled(
            Booking booking
    ) {

        return new BookingCancelledEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId()

        );

    }

    public JobCompletionConfirmedEvent jobCompletionConfirmed(
            Booking booking
    ) {

        return new JobCompletionConfirmedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId()

        );

    }

    public MaterialReleaseRequestedEvent materialReleaseRequested(

            Booking booking,

            BookingQuote quote

    ) {
        return new MaterialReleaseRequestedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getProviderUserId(),

                quote.getId(),

                quote.getMaterialsAmount()

        );

    }

    public BookingStatusChangedEvent statusChanged(

            Booking booking,

            BookingStatus previousStatus

    ) {

        return new BookingStatusChangedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId(),

                previousStatus,

                booking.getStatus()

        );

    }

    public BookingPaymentCompletedEvent paymentCompleted(
            Booking booking
    ) {

        return new BookingPaymentCompletedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId()

        );

    }

    public BookingUpdatedEvent bookingUpdated(
            Booking booking
    ) {

        return new BookingUpdatedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId()

        );

    }
}