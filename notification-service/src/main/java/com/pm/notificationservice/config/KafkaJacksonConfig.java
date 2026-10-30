package com.pm.notificationservice.config;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.pm.contracts.admin.AdminNotificationEvent;
import com.pm.contracts.booking.*;
import com.pm.contracts.chat.ChatMessageReceivedEvent;
import com.pm.contracts.escrow.*;
import com.pm.contracts.payment.*;
import com.pm.contracts.rating.CustomerRatedEvent;
import com.pm.contracts.rating.ProviderRatedEvent;
import com.pm.contracts.system.SystemNotificationEvent;
import com.pm.contracts.wallet.*;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaJacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer kafkaEventCustomizer() {

        return builder -> builder.postConfigurer(mapper -> {

            /*
             * Booking Events
             */
            mapper.registerSubtypes(
                    new NamedType(BookingCreatedEvent.class, "BOOKING_CREATED"),
                    new NamedType(BookingAcceptedEvent.class, "BOOKING_ACCEPTED"),
                    new NamedType(BookingRejectedEvent.class, "BOOKING_REJECTED"),
                    new NamedType(BookingCancelledEvent.class, "BOOKING_CANCELLED"),
                    new NamedType(QuoteReceivedEvent.class, "QUOTE_RECEIVED"),
                    new NamedType(QuoteAcceptedEvent.class, "QUOTE_ACCEPTED"),
                    new NamedType(QuoteRejectedEvent.class, "QUOTE_REJECTED"),
                    new NamedType(BookingStartedEvent.class, "BOOKING_STARTED"),
                    new NamedType(BookingCompletedEvent.class, "BOOKING_COMPLETED"),
                    new NamedType(CustomerConfirmedCompletionEvent.class, "CUSTOMER_CONFIRMED_COMPLETION")
            );

            /*
             * Payment Events
             */
            mapper.registerSubtypes(
                    new NamedType(PaymentPendingEvent.class, "PAYMENT_PENDING"),
                    new NamedType(PaymentCompletedEvent.class, "PAYMENT_COMPLETED"),
                    new NamedType(PaymentFailedEvent.class, "PAYMENT_FAILED")
            );

            /*
             * Escrow Events
             */
            mapper.registerSubtypes(
                    new NamedType(EscrowCreatedEvent.class, "ESCROW_CREATED"),
                    new NamedType(MaterialsReleasedEvent.class, "MATERIALS_RELEASED"),
                    new NamedType(LabourReleasedEvent.class, "LABOUR_RELEASED"),
                    new NamedType(EscrowRefundedEvent.class, "ESCROW_REFUNDED")
            );

            /*
             * Wallet Events
             */
            mapper.registerSubtypes(
                    new NamedType(WalletCreditedEvent.class, "WALLET_CREDITED"),
                    new NamedType(WithdrawalRequestedEvent.class, "WITHDRAWAL_REQUESTED"),
                    new NamedType(WithdrawalCompletedEvent.class, "WITHDRAWAL_COMPLETED"),
                    new NamedType(WithdrawalFailedEvent.class, "WITHDRAWAL_FAILED")
            );

            /*
             * Rating Events
             */
            mapper.registerSubtypes(
                    new NamedType(ProviderRatedEvent.class, "PROVIDER_RATED"),
                    new NamedType(CustomerRatedEvent.class, "CUSTOMER_RATED")
            );

            /*
             * Chat Events
             */
            mapper.registerSubtypes(
                    new NamedType(ChatMessageReceivedEvent.class, "CHAT_MESSAGE_RECEIVED")
            );

            /*
             * Notification Events
             */
            mapper.registerSubtypes(
                    new NamedType(SystemNotificationEvent.class, "SYSTEM_NOTIFICATION"),
                    new NamedType(AdminNotificationEvent.class, "ADMIN_NOTIFICATION")
            );

        });

    }

}