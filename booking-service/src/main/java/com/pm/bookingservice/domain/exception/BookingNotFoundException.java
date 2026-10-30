package com.pm.bookingservice.domain.exception;

import java.util.UUID;

public class BookingNotFoundException extends BookingException {

    public BookingNotFoundException(UUID bookingId) {
        super("Booking not found with id: " + bookingId);
    }

    public BookingNotFoundException(String message) {
        super(message);
    }
}