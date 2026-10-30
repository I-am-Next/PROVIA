package com.pm.paymentservicenew.integration.paystack.dto;

public record PaystackWebhookData(

        String reference,

        String status

) {
}