package com.pm.bookingservice.application.mapper;

import com.pm.bookingservice.application.dto.query.BookingFilter;
import com.pm.bookingservice.domain.model.BookingStatus;

import java.util.EnumSet;
import java.util.Set;

public final class BookingFilterMapper {

    private BookingFilterMapper() {
    }

    public static Set<BookingStatus> toStatuses(
            BookingFilter filter
    ) {

        return switch (filter) {

            case AWAITING_QUOTE ->
                    EnumSet.of(
                            BookingStatus.REQUESTED,
                            BookingStatus.AWAITING_QUOTE
                    );

            case QUOTED ->
                    EnumSet.of(
                            BookingStatus.QUOTE_SENT,
                            BookingStatus.QUOTE_ACCEPTED
                    );

            case IN_PROGRESS ->
                    EnumSet.of(
                            BookingStatus.PAYMENT_COMPLETED,
                            BookingStatus.MATERIALS_RELEASED,
                            BookingStatus.IN_PROGRESS
                    );

            case COMPLETED ->
                    EnumSet.of(
                            BookingStatus.COMPLETED,
                            BookingStatus.COMPLETION_CONFIRMED
                    );

            case CANCELLED ->
                    EnumSet.of(
                            BookingStatus.QUOTE_REJECTED,
                            BookingStatus.REJECTED,
                            BookingStatus.CANCELLED
                    );

            case ALL ->
                    EnumSet.allOf(BookingStatus.class);
        };
    }

}