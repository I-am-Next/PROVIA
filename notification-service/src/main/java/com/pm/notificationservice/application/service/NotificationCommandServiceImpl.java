package com.pm.notificationservice.application.service;

import com.pm.notificationservice.domain.exception.NotificationNotFoundException;
import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationCommandServiceImpl
        implements NotificationCommandService {

    private final NotificationRepository notificationRepository;

    public NotificationCommandServiceImpl(

            NotificationRepository notificationRepository

    ) {

        this.notificationRepository = notificationRepository;

    }

    @Override
    public void markAsRead(

            UUID notificationId,

            UUID recipientUserId

    ) {

        Notification notification = getOwnedNotification(

                notificationId,
                recipientUserId

        );

        notification.markAsRead();

        notificationRepository.save(

                notification

        );

    }

    @Override
    public void markAllAsRead(

            UUID recipientUserId

    ) {

        List<Notification> notifications =
                notificationRepository.findByRecipientUserIdAndReadFalse(

                        recipientUserId

                );

        notifications.forEach(

                Notification::markAsRead

        );

        notificationRepository.saveAll(

                notifications

        );

    }

    @Override
    public void deleteNotification(

            UUID notificationId,

            UUID recipientUserId

    ) {

        Notification notification = getOwnedNotification(

                notificationId,
                recipientUserId

        );

        notificationRepository.delete(

                notification

        );

    }

    @Override
    public void deleteAllNotifications(

            UUID recipientUserId

    ) {

        notificationRepository.deleteByRecipientUserId(

                recipientUserId

        );

    }

    private Notification getOwnedNotification(

            UUID notificationId,

            UUID recipientUserId

    ) {

        return notificationRepository
                .findByIdAndRecipientUserId(

                        notificationId,

                        recipientUserId

                )
                .orElseThrow(() ->

                        new NotificationNotFoundException(

                                notificationId

                        )

                );

    }
}