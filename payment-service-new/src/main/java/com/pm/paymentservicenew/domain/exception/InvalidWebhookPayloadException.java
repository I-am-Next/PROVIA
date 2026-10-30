package com.pm.paymentservicenew.domain.exception;

public class InvalidWebhookPayloadException extends RuntimeException {

    public InvalidWebhookPayloadException() {

        super("Invalid Paystack webhook payload.");

    }
    public InvalidWebhookPayloadException(

            Throwable cause

    ) {
        super("Invalid Paystack webhook payload.", cause);

    }

}