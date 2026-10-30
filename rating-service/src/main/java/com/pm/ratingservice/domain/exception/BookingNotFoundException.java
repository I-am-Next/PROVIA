package com.pm.ratingservice.domain.exception;

import java.util.UUID;

public class BookingNotFoundException extends RatingException {

    public BookingNotFoundException(UUID bookingId) {
        super("Booking not found: " + bookingId);
    }
}