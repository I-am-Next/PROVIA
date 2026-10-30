package com.pm.bookingservice.application.dto.query;

import com.pm.bookingservice.domain.model.BookingStatus;

import java.util.Set;

public enum BookingFilter {

    ALL(Set.of(BookingStatus.values())),

    AWAITING_QUOTE(Set.of(
            BookingStatus.REQUESTED,
            BookingStatus.AWAITING_QUOTE
    )),

    QUOTED(Set.of(
            BookingStatus.QUOTE_SENT,
            BookingStatus.QUOTE_ACCEPTED
    )),

    IN_PROGRESS(Set.of(
            BookingStatus.PAYMENT_COMPLETED,
            BookingStatus.MATERIALS_RELEASED,
            BookingStatus.IN_PROGRESS
    )),

    COMPLETED(Set.of(
            BookingStatus.COMPLETED,
            BookingStatus.COMPLETION_CONFIRMED
    )),

    CANCELLED(Set.of(
            BookingStatus.QUOTE_REJECTED,
            BookingStatus.REJECTED,
            BookingStatus.CANCELLED
    ));

    private final Set<BookingStatus> statuses;

    BookingFilter(Set<BookingStatus> statuses) {
        this.statuses = statuses;
    }

    public Set<BookingStatus> statuses() {
        return statuses;
    }
}