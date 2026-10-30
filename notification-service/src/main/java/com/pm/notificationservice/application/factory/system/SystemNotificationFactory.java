package com.pm.notificationservice.application.factory.system;

import com.pm.notificationservice.application.factory.shared.BaseNotificationFactory;
import com.pm.notificationservice.application.factory.shared.NotificationActionUrlFactory;
import com.pm.notificationservice.application.factory.shared.NotificationMetadataFactory;
import com.pm.notificationservice.application.factory.shared.NotificationPriorityFactory;
import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import com.pm.contracts.common.enums.RecipientRole;
import org.springframework.stereotype.Component;

@Component
public class SystemNotificationFactory extends BaseNotificationFactory {

    public SystemNotificationFactory(

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

    public Notification systemNotification(

            java.util.UUID recipientUserId,

            RecipientRole recipientRole,

            String title,

            String message,

            String actionUrl,

            String metadata

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.SYSTEM,

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