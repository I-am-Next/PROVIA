package com.pm.bookingservice.domain.exception;

public class BookingAccessDeniedException extends BookingException {

    public BookingAccessDeniedException() {
        super("You do not have permission to access this booking.");
    }

    public BookingAccessDeniedException(String message) {
        super(message);
    }
}