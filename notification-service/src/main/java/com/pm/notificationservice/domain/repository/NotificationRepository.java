package com.pm.notificationservice.domain.repository;

import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(

            UUID recipientUserId,

            Pageable pageable

    );

    Page<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(

            UUID recipientUserId,

            Pageable pageable

    );

    long countByRecipientUserIdAndReadFalse(

            UUID recipientUserId

    );

    Page<Notification> findByRecipientUserIdAndNotificationTypeOrderByCreatedAtDesc(

            UUID recipientUserId,

            NotificationType notificationType,

            Pageable pageable

    );

    Page<Notification> findByRecipientUserIdAndReadOrderByCreatedAtDesc(

            UUID recipientUserId,

            boolean read,

            Pageable pageable

    );

    Optional<Notification> findByIdAndRecipientUserId(

            UUID notificationId,

            UUID recipientUserId

    );

    List<Notification> findByRecipientUserIdAndReadFalse(

            UUID recipientUserId

    );

    void deleteByRecipientUserId(

            UUID recipientUserId

    );

}