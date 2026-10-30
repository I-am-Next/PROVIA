package com.pm.paymentservicenew.integration.paystack.dto;

public record VerifyTransactionResponse(

        long amount,

        String currency,

        String status,

        String reference,

        String paid_at,

        String gateway_response

) {
}