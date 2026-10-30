package com.pm.notificationservice.infrastructure.consumer.booking;

import com.pm.contracts.booking.BookingAcceptedEvent;
import com.pm.contracts.booking.BookingCancelledEvent;
import com.pm.contracts.booking.BookingCompletedEvent;
import com.pm.contracts.booking.BookingCreatedEvent;
import com.pm.contracts.booking.BookingPaymentCompletedEvent;
import com.pm.contracts.booking.BookingRejectedEvent;
import com.pm.contracts.booking.BookingStartedEvent;
import com.pm.contracts.booking.BookingUpdatedEvent;
import com.pm.contracts.booking.CustomerConfirmedCompletionEvent;
import com.pm.contracts.common.DomainEvent;
import org.springframework.stereotype.Component;

@Component
public class EventTypeResolver {

    public boolean supports(String eventType) {
        return switch (eventType) {
            case "BOOKING_CREATED",
                 "BOOKING_UPDATED",
                 "BOOKING_PAYMENT_COMPLETED",
                 "BOOKING_ACCEPTED",
                 "BOOKING_REJECTED",
                 "BOOKING_CANCELLED",
                 "BOOKING_STARTED",
                 "BOOKING_COMPLETED",
                 "CUSTOMER_CONFIRMED_COMPLETION" -> true;

            default -> false;
        };
    }

    public Class<? extends DomainEvent> resolve(String eventType) {
        return switch (eventType) {
            case "BOOKING_CREATED" ->
                    BookingCreatedEvent.class;
            case "BOOKING_UPDATED" ->
                    BookingUpdatedEvent.class;
            case "BOOKING_PAYMENT_COMPLETED" ->
                    BookingPaymentCompletedEvent.class;
            case "BOOKING_ACCEPTED" ->
                    BookingAcceptedEvent.class;
            case "BOOKING_REJECTED" ->
                    BookingRejectedEvent.class;
            case "BOOKING_CANCELLED" ->
                    BookingCancelledEvent.class;
            case "BOOKING_STARTED" ->
                    BookingStartedEvent.class;
            case "BOOKING_COMPLETED" ->
                    BookingCompletedEvent.class;
            case "CUSTOMER_CONFIRMED_COMPLETION" ->
                    CustomerConfirmedCompletionEvent.class;

            default -> throw new IllegalArgumentException(
                    "Unsupported booking event type: " + eventType
            );
        };
    }
}
