package com.pm.paymentservicenew.integration.paystack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paystack")
public record PaystackProperties(

        String secretKey,

        String publicKey,

        String baseUrl,

        String callbackUrl

) {
}