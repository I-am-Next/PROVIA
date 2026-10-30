package com.pm.notificationservice.api.dto.response;

import com.pm.notificationservice.domain.model.enums.NotificationPriority;
import com.pm.notificationservice.domain.model.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationSummaryResponse(

        UUID id,

        NotificationType notificationType,

        NotificationPriority priority,

        String title,

        String message,

        boolean read,

        String actionUrl,

        Instant createdAt

) {
}