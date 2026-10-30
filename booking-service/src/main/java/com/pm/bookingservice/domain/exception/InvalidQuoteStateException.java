package com.pm.bookingservice.domain.exception;

public class InvalidQuoteStateException extends BookingException {

    public InvalidQuoteStateException(
            String message
    ) {

        super(message);

    }

}