package com.pm.paymentservicenew.domain.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(
            UUID id,
            String lookupType
    ) {

        super(

                String.format(

                        "Payment not found for %s: %s.",

                        lookupType,

                        id

                )

        );

    }

}