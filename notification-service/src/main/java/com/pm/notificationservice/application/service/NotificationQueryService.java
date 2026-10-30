package com.pm.notificationservice.application.service;

import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationQueryService {

    Page<Notification> getNotifications(

            UUID recipientUserId,

            Pageable pageable

    );

    Page<Notification> getUnreadNotifications(

            UUID recipientUserId,

            Pageable pageable

    );

    Page<Notification> getNotificationsByType(

            UUID recipientUserId,

            NotificationType notificationType,

            Pageable pageable

    );

    Page<Notification> getNotificationsByReadStatus(

            UUID recipientUserId,

            boolean read,

            Pageable pageable

    );

    long getUnreadCount(

            UUID recipientUserId

    );

}