package com.pm.bookingservice.infrastructure.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor internalServiceRequestInterceptor(
            @Value("${internal.service-key}")
            String internalServiceKey
    ) {
        return requestTemplate -> requestTemplate.header(
                "X-Internal-Service-Key",
                internalServiceKey
        );
    }
}