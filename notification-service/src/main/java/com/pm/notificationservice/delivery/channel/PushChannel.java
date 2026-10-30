package com.pm.notificationservice.delivery.channel;

import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PushChannel implements NotificationChannel {

    private static final Logger log =
            LoggerFactory.getLogger(PushChannel.class);

    public PushChannel() {
    }

    @Override
    public NotificationChannelType getChannelType() {
        return NotificationChannelType.PUSH;
    }

    @Override
    public void send(
            Notification notification,
            String subject,
            String message
    ) {

        /*
         * Placeholder implementation.
         *
         * This will later integrate with a push notification
         * provider such as Firebase Cloud Messaging (FCM),
         * OneSignal or Apple Push Notification Service (APNs).
         */

        log.info(
                "Sending PUSH notification."
        );

    }

}