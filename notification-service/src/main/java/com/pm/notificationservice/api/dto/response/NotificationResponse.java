package com.pm.notificationservice.api.dto.response;

import com.pm.contracts.common.enums.RecipientRole;
import com.pm.notificationservice.domain.model.enums.NotificationPriority;
import com.pm.notificationservice.domain.model.enums.NotificationType;


import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(

        UUID id,

        UUID recipientUserId,

        RecipientRole recipientRole,

        NotificationType notificationType,

        NotificationPriority priority,

        String title,

        String message,

        UUID bookingId,

        UUID paymentId,

        UUID escrowId,

        String actionUrl,

        String metadata,

        boolean read,

        Instant createdAt,

        Instant readAt

) {
}