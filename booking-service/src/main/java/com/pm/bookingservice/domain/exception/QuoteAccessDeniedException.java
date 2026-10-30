package com.pm.bookingservice.domain.exception;

public class QuoteAccessDeniedException extends BookingException {

    public QuoteAccessDeniedException(
            String message
    ) {

        super(message);

    }

}