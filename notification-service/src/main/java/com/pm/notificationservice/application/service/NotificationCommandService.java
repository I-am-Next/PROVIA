package com.pm.notificationservice.application.service;

import java.util.UUID;

public interface NotificationCommandService {

    void markAsRead(

            UUID notificationId,

            UUID recipientUserId

    );

    void markAllAsRead(

            UUID recipientUserId

    );

    void deleteNotification(

            UUID notificationId,

            UUID recipientUserId

    );

    void deleteAllNotifications(

            UUID recipientUserId

    );

}