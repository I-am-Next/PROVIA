package com.pm.paymentservicenew.integration.paystack.dto;

public record PaystackResponse<T>(

        boolean status,

        String message,

        T data

) {
}