package com.pm.paymentservicenew.integration.paystack.client;

import com.pm.paymentservicenew.integration.paystack.config.PaystackProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PaystackClientConfig {

    @Bean
    public RestClient paystackRestClient(PaystackProperties properties) {

        System.out.println("========== PAYSTACK CONFIG ==========");
        System.out.println("Base URL : " + properties.baseUrl());
        System.out.println("Callback : " + properties.callbackUrl());
        System.out.println("=====================================");

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(
                        "Authorization",
                        "Bearer " + properties.secretKey()
                )
                .defaultHeader(
                        "Content-Type",
                        "application/json"
                )
                .build();
    }

}