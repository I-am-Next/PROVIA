package com.pm.paymentservicenew.domain.exception;

public class PaymentVerificationFailedException extends RuntimeException {

    public PaymentVerificationFailedException() {

        super(

                "Unable to verify payment with Paystack."

        );

    }

    public PaymentVerificationFailedException(

            Throwable cause

    ) {

        super(

                "Unable to verify payment with Paystack.",

                cause

        );

    }

}