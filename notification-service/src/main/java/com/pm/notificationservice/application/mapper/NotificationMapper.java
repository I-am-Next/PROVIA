package com.pm.notificationservice.application.mapper;

import com.pm.notificationservice.api.dto.response.NotificationResponse;
import com.pm.notificationservice.api.dto.response.NotificationSummaryResponse;
import com.pm.notificationservice.domain.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(

            Notification notification

    ) {

        return new NotificationResponse(

                notification.getId(),

                notification.getRecipientUserId(),

                notification.getRecipientRole(),

                notification.getNotificationType(),

                notification.getPriority(),

                notification.getTitle(),

                notification.getMessage(),

                notification.getBookingId(),

                notification.getPaymentId(),

                notification.getEscrowId(),

                notification.getActionUrl(),

                notification.getMetadata(),

                notification.isRead(),

                notification.getCreatedAt(),

                notification.getReadAt()

        );

    }

    public NotificationSummaryResponse toSummary(

            Notification notification

    ) {

        return new NotificationSummaryResponse(

                notification.getId(),

                notification.getNotificationType(),

                notification.getPriority(),

                notification.getTitle(),

                notification.getMessage(),

                notification.isRead(),

                notification.getActionUrl(),

                notification.getCreatedAt()

        );

    }

}