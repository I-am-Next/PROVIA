package com.pm.paymentservicenew.application.service;

public interface PaymentWebhookService {

    void handleWebhook(

            String payload,

            String signature

    );

}