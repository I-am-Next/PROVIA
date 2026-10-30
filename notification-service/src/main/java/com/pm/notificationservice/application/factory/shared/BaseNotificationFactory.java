package com.pm.notificationservice.application.factory.shared;

import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import com.pm.contracts.common.enums.RecipientRole;

import java.util.UUID;

public abstract class BaseNotificationFactory {

    protected final NotificationMetadataFactory metadataFactory;

    protected final NotificationActionUrlFactory actionUrlFactory;

    protected final NotificationPriorityFactory priorityFactory;

    protected BaseNotificationFactory(

            NotificationMetadataFactory metadataFactory,

            NotificationActionUrlFactory actionUrlFactory,

            NotificationPriorityFactory priorityFactory

    ) {

        this.metadataFactory = metadataFactory;
        this.actionUrlFactory = actionUrlFactory;
        this.priorityFactory = priorityFactory;

    }

    protected Notification createNotification(

            UUID recipientUserId,

            RecipientRole recipientRole,

            NotificationType notificationType,

            String title,

            String message,

            UUID bookingId,

            UUID paymentId,

            UUID escrowId,

            String actionUrl,

            String metadata

    ) {

        return Notification.create(

                recipientUserId,

                recipientRole,

                notificationType,

                priorityFactory.priorityFor(

                        notificationType

                ),

                title,

                message,

                bookingId,

                paymentId,

                escrowId,

                actionUrl,

                metadata

        );

    }

}