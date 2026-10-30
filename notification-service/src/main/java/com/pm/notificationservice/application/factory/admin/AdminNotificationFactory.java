package com.pm.notificationservice.application.factory.admin;

import com.pm.notificationservice.application.factory.shared.BaseNotificationFactory;
import com.pm.notificationservice.application.factory.shared.NotificationActionUrlFactory;
import com.pm.notificationservice.application.factory.shared.NotificationMetadataFactory;
import com.pm.notificationservice.application.factory.shared.NotificationPriorityFactory;
import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import com.pm.contracts.common.enums.RecipientRole;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminNotificationFactory extends BaseNotificationFactory {

    public AdminNotificationFactory(

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

    public Notification adminNotification(

            UUID recipientUserId,

            RecipientRole recipientRole,

            String title,

            String message,

            String actionUrl,

            String metadata

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.ADMIN,

                title,

                message,

                null,

                null,

                null,

                actionUrl,

                metadata

        );

    }

}