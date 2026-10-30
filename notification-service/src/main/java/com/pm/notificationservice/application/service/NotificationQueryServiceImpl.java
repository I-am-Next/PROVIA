package com.pm.notificationservice.application.service;

import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import com.pm.notificationservice.domain.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl
        implements NotificationQueryService {

    private final NotificationRepository notificationRepository;

    public NotificationQueryServiceImpl(

            NotificationRepository notificationRepository

    ) {

        this.notificationRepository = notificationRepository;

    }

    @Override
    public Page<Notification> getNotifications(

            UUID recipientUserId,

            Pageable pageable

    ) {

        return notificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(

                        recipientUserId,

                        pageable

                );

    }

    @Override
    public Page<Notification> getUnreadNotifications(

            UUID recipientUserId,

            Pageable pageable

    ) {

        return notificationRepository
                .findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(

                        recipientUserId,

                        pageable

                );

    }

    @Override
    public Page<Notification> getNotificationsByType(

            UUID recipientUserId,

            NotificationType notificationType,

            Pageable pageable

    ) {

        return notificationRepository
                .findByRecipientUserIdAndNotificationTypeOrderByCreatedAtDesc(

                        recipientUserId,

                        notificationType,

                        pageable

                );

    }

    @Override
    public Page<Notification> getNotificationsByReadStatus(

            UUID recipientUserId,

            boolean read,

            Pageable pageable

    ) {

        return notificationRepository
                .findByRecipientUserIdAndReadOrderByCreatedAtDesc(

                        recipientUserId,

                        read,

                        pageable

                );

    }

    @Override
    public long getUnreadCount(

            UUID recipientUserId

    ) {

        return notificationRepository
                .countByRecipientUserIdAndReadFalse(

                        recipientUserId

                );

    }

}