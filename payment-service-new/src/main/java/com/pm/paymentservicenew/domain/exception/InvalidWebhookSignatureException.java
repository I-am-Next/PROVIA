package com.pm.paymentservicenew.domain.exception;

public class InvalidWebhookSignatureException extends RuntimeException {

    public InvalidWebhookSignatureException() {

        super(

                "Invalid Paystack webhook signature."

        );

    }

    public InvalidWebhookSignatureException(

            Throwable cause

    ) {

        super(

                "Invalid Paystack webhook signature.",

                cause

        );

    }

}