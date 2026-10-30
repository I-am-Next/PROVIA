package com.pm.bookingservice.domain.exception;

import java.util.UUID;

public class QuoteNotFoundException extends BookingException {

    public QuoteNotFoundException(UUID bookingId) {

        super(

                String.format(
                        "Quote for booking %s not found",
                        bookingId
                )

        );
    }

}