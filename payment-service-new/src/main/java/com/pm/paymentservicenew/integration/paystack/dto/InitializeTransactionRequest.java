package com.pm.paymentservicenew.integration.paystack.dto;

public record InitializeTransactionRequest(

        String email,

        long amount,

        String reference,

        String callback_url

) {
}