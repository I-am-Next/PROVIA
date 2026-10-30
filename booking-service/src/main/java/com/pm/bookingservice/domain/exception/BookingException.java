package com.pm.bookingservice.domain.exception;

public abstract class BookingException extends RuntimeException {

    protected BookingException(String message) {
        super(message);
    }

    protected BookingException(String message, Throwable cause) {
        super(message, cause);
    }
}