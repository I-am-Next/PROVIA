package com.pm.bookingservice.domain.exception;

public class DuplicateQuoteException extends BookingException {

    public DuplicateQuoteException() {

        super(

                "Booking already has an active quote"

        );

    }

}