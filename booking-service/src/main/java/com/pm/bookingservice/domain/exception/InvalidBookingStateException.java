package com.pm.bookingservice.domain.exception;

public class InvalidBookingStateException extends BookingException {

    public InvalidBookingStateException(String message) {
        super(message);
    }
}