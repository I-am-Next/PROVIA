package com.pm.paymentservicenew.application.service;

public interface IdempotencyService {

    void registerEvent(

            String provider,

            String idempotencyKey,

            String eventType

    );

}