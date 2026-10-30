package com.pm.notificationservice.application.service;

import com.pm.notificationservice.domain.exception.NotificationNotFoundException;
import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationPriority;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import com.pm.contracts.common.enums.RecipientRole;
import com.pm.notificationservice.domain.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class NotificationCommandServiceImplTest {

    private NotificationRepository notificationRepository;

    private NotificationCommandServiceImpl service;

    @BeforeEach
    void setUp() {

        notificationRepository =
                mock(NotificationRepository.class);

        service =
                new NotificationCommandServiceImpl(
                        notificationRepository
                );
    }

    @Test
    void markAsRead_rejectsNotificationOwnedByAnotherUser() {

        UUID notificationId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        when(notificationRepository.findByIdAndRecipientUserId(
                notificationId,
                authenticatedUserId
        )).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() ->
                service.markAsRead(
                        notificationId,
                        authenticatedUserId
                )
        )
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository)
                .findByIdAndRecipientUserId(
                        notificationId,
                        authenticatedUserId
                );

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }

    @Test
    void deleteNotification_rejectsNotificationOwnedByAnotherUser() {

        UUID notificationId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        when(notificationRepository.findByIdAndRecipientUserId(
                notificationId,
                authenticatedUserId
        )).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() ->
                service.deleteNotification(
                        notificationId,
                        authenticatedUserId
                )
        )
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository)
                .findByIdAndRecipientUserId(
                        notificationId,
                        authenticatedUserId
                );

        verify(notificationRepository, never())
                .delete(any(Notification.class));
    }

    @Test
    void markAsRead_marksOwnedNotificationAsRead() {

        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Notification notification =
                Notification.create(
                        userId,
                        RecipientRole.CUSTOMER,
                        NotificationType.SYSTEM,
                        NotificationPriority.NORMAL,
                        "Test notification",
                        "Test message",
                        null,
                        null,
                        null,
                        "/notifications",
                        null
                );

        when(notificationRepository.findByIdAndRecipientUserId(
                notificationId,
                userId
        )).thenReturn(java.util.Optional.of(notification));

        service.markAsRead(
                notificationId,
                userId
        );

        assertThat(notification.isRead())
                .isTrue();

        assertThat(notification.getReadAt())
                .isNotNull();

        verify(notificationRepository)
                .save(notification);
    }

    @Test
    void markAsRead_isIdempotentForAlreadyReadNotification() {

        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Notification notification =
                Notification.create(
                        userId,
                        RecipientRole.CUSTOMER,
                        NotificationType.SYSTEM,
                        NotificationPriority.NORMAL,
                        "Test notification",
                        "Test message",
                        null,
                        null,
                        null,
                        "/notifications",
                        null
                );

        notification.markAsRead();

        var originalReadAt =
                notification.getReadAt();

        when(notificationRepository.findByIdAndRecipientUserId(
                notificationId,
                userId
        )).thenReturn(java.util.Optional.of(notification));

        service.markAsRead(
                notificationId,
                userId
        );

        assertThat(notification.isRead())
                .isTrue();

        assertThat(notification.getReadAt())
                .isEqualTo(originalReadAt);

        verify(notificationRepository)
                .save(notification);
    }
}