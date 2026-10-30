package com.pm.notificationservice.application.factory.chat;

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

import java.util.UUID;

@Component
public class ChatNotificationFactory extends BaseNotificationFactory {

    public ChatNotificationFactory(

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

    public Notification chatMessage(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID conversationId,

            UUID senderId,

            String senderName

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.CHAT_MESSAGE,

                NotificationTitles.CHAT_MESSAGE,

                NotificationMessages.chatMessage(

                        senderName

                ),

                null,

                null,

                null,

                actionUrlFactory.chat(

                        conversationId

                ),

                metadataFactory.chatMetadata(

                        conversationId,

                        senderId

                )

        );

    }

}