package com.pm.paymentservicenew.domain.exception;

import java.util.UUID;

public class PaymentAlreadyExistsException extends RuntimeException {

    public PaymentAlreadyExistsException(

            UUID bookingId

    ) {

        super(

                "A payment already exists for booking: " + bookingId

        );

    }

}
