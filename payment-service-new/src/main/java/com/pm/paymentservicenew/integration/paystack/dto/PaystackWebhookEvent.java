package com.pm.paymentservicenew.integration.paystack.dto;

public record PaystackWebhookEvent(

        String event,

        PaystackWebhookData data

) {
}