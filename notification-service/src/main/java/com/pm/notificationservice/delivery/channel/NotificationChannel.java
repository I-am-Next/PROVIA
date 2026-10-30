package com.pm.notificationservice.delivery.channel;

import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.NotificationChannelType;

public interface NotificationChannel {

    NotificationChannelType getChannelType();

    void send(
            Notification notification,
            String subject,
            String message
    );

}