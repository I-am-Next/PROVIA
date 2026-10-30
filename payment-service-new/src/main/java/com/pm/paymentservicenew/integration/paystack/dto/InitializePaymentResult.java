package com.pm.paymentservicenew.integration.paystack.dto;

public record InitializePaymentResult(

        String authorizationUrl,

        String reference

) {
}