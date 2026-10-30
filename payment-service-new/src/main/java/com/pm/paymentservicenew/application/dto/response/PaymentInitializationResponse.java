package com.pm.paymentservicenew.application.dto.response;

public record PaymentInitializationResponse(

        PaymentResponse payment,

        String authorizationUrl,

        String reference

) {
}