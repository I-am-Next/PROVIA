package com.pm.paymentservicenew.integration.paystack.security;

public interface PaystackWebhookVerifier {

    void verify(

            String payload,

            String signature

    );

}