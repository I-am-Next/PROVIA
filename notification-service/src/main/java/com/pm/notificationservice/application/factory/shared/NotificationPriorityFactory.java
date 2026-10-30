package com.pm.notificationservice.application.factory.shared;

import com.pm.notificationservice.domain.model.enums.NotificationPriority;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class NotificationPriorityFactory {

    public NotificationPriority priorityFor(

            NotificationType notificationType

    ) {

        return switch (notificationType) {

            /*
             * Critical platform events
             */
            case PAYMENT_FAILED,
                 ADMIN ->
                    NotificationPriority.CRITICAL;

            /*
             * High importance business events
             */
            case QUOTE_RECEIVED,
                 QUOTE_ACCEPTED,
                 QUOTE_REJECTED,

                 PAYMENT_COMPLETED,

                 MATERIALS_RELEASED,
                 LABOUR_RELEASED,
                 ESCROW_REFUNDED,

                 WORK_COMPLETED,
                 CUSTOMER_CONFIRMED_COMPLETION,

                 WITHDRAWAL_FAILED ->
                    NotificationPriority.HIGH;

            /*
             * Normal day-to-day events
             */
            case BOOKING_CREATED,
                 BOOKING_ACCEPTED,
                 BOOKING_REJECTED,
                 BOOKING_CANCELLED,

                 PAYMENT_PENDING,

                 ESCROW_CREATED,

                 WORK_STARTED,

                 WALLET_CREDITED,
                 WITHDRAWAL_REQUESTED,
                 WITHDRAWAL_COMPLETED,

                 PROVIDER_RATED,
                 CUSTOMER_RATED,

                 CHAT_MESSAGE,

                 SYSTEM ->
                    NotificationPriority.NORMAL;

        };

    }

}