package com.pm.notificationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.kafka")
public record NotificationKafkaProperties(

        String bookings,

        String quotes,

        String payments,

        String escrows,

        String wallets,

        String ratings,

        String chats,

        String system,

        String admin

) {
}