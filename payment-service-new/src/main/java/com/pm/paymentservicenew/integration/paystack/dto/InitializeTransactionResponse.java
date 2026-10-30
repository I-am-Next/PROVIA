package com.pm.paymentservicenew.integration.paystack.dto;

public record InitializeTransactionResponse(

        String authorization_url,

        String access_code,

        String reference

) {
}