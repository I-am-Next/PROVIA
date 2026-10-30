package com.pm.notificationservice.application.factory.payment;

import com.pm.notificationservice.application.factory.shared.BaseNotificationFactory;
import com.pm.notificationservice.application.factory.shared.NotificationActionUrlFactory;
import com.pm.notificationservice.application.factory.shared.NotificationMessages;
import com.pm.notificationservice.application.factory.shared.NotificationMetadataFactory;
import com.pm.notificationservice.application.factory.shared.NotificationPriorityFactory;
import com.pm.notificationservice.application.factory.shared.NotificationTitles;
import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import com.pm.contracts.common.enums.RecipientRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentNotificationFactory extends BaseNotificationFactory {

    public PaymentNotificationFactory(

            NotificationMetadataFactory metadataFactory,

            NotificationActionUrlFactory actionUrlFactory,

            NotificationPriorityFactory priorityFactory

    ) {

        super(

                metadataFactory,

                actionUrlFactory,

                priorityFactory

        );

    }

    public Notification paymentPending(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId,

            UUID paymentId,

            BigDecimal amount,

            String currency

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.PAYMENT_PENDING,

                NotificationTitles.PAYMENT_PENDING,

                NotificationMessages.paymentPending(

                        amount,

                        currency

                ),

                bookingId,

                paymentId,

                null,

                actionUrlFactory.payment(

                        paymentId

                ),

                metadataFactory.paymentMetadata(

                        bookingId,

                        paymentId

                )

        );

    }

    public Notification paymentCompleted(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId,

            UUID paymentId,

            BigDecimal amount,

            String currency

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.PAYMENT_COMPLETED,

                NotificationTitles.PAYMENT_COMPLETED,

                NotificationMessages.paymentCompleted(

                        amount,

                        currency

                ),

                bookingId,

                paymentId,

                null,

                actionUrlFactory.payment(

                        paymentId

                ),

                metadataFactory.paymentMetadata(

                        bookingId,

                        paymentId

                )

        );

    }

    public Notification paymentFailed(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId,

            UUID paymentId,

            BigDecimal amount,

            String currency,

            String reason

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.PAYMENT_FAILED,

                NotificationTitles.PAYMENT_FAILED,

                NotificationMessages.paymentFailed(

                        amount,

                        currency,

                        reason

                ),

                bookingId,

                paymentId,

                null,

                actionUrlFactory.payment(

                        paymentId

                ),

                metadataFactory.paymentMetadata(

                        bookingId,

                        paymentId

                )

        );

    }

}