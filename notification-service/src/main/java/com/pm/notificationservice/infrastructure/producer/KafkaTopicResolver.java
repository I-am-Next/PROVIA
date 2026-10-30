package com.pm.notificationservice.infrastructure.producer;

import com.pm.notificationservice.config.NotificationKafkaProperties;
import com.pm.notificationservice.infrastructure.outbox.NotificationOutboxEventType;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopicResolver {

    private final NotificationKafkaProperties properties;

    public KafkaTopicResolver(
            NotificationKafkaProperties properties
    ) {
        this.properties = properties;
    }

    public String resolve(
            NotificationOutboxEventType eventType
    ) {

        return switch (eventType) {

            /*
             * Booking
             */
            case BOOKING_CREATED,
                 BOOKING_ACCEPTED,
                 BOOKING_REJECTED,
                 BOOKING_CANCELLED,
                 WORK_STARTED,
                 WORK_COMPLETED,
                 CUSTOMER_CONFIRMED_COMPLETION ->
                    properties.bookings();

            /*
             * Quote
             */
            case QUOTE_RECEIVED,
                 QUOTE_ACCEPTED,
                 QUOTE_REJECTED ->
                    properties.quotes();

            /*
             * Payment
             */
            case PAYMENT_PENDING,
                 PAYMENT_COMPLETED,
                 PAYMENT_FAILED ->
                    properties.payments();

            /*
             * Escrow
             */
            case ESCROW_CREATED,
                 MATERIALS_RELEASED,
                 LABOUR_RELEASED,
                 ESCROW_REFUNDED ->
                    properties.escrows();

            /*
             * Wallet
             */
            case WALLET_CREDITED,
                 WITHDRAWAL_REQUESTED,
                 WITHDRAWAL_COMPLETED,
                 WITHDRAWAL_FAILED ->
                    properties.wallets();

            /*
             * Rating
             */
            case PROVIDER_RATED,
                 CUSTOMER_RATED ->
                    properties.ratings();

            /*
             * Chat
             */
            case CHAT_MESSAGE_RECEIVED ->
                    properties.chats();

            /*
             * System
             */
            case SYSTEM_NOTIFICATION ->
                    properties.system();

            /*
             * Admin
             */
            case ADMIN_NOTIFICATION ->
                    properties.admin();

        };

    }

}