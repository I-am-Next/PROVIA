package com.pm.paymentservicenew.domain.exception;

public class DuplicateWebhookEventException extends RuntimeException {

    public DuplicateWebhookEventException() {

        super("Webhook event has already been processed.");

    }

}