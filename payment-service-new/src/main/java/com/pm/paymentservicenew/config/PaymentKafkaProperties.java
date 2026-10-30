package com.pm.paymentservicenew.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.kafka.topics")
public record PaymentKafkaProperties(

        String payments,

        String escrows,

        String wallets

) {
}