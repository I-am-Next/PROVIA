package com.pm.notificationservice.delivery.channel;

import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.NotificationChannelType;
import com.pm.notificationservice.domain.repository.NotificationRepository;
import org.springframework.stereotype.Component;

@Component
public class InAppChannel implements NotificationChannel {

    private final NotificationRepository notificationRepository;

    public InAppChannel(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationChannelType getChannelType() {
        return NotificationChannelType.IN_APP;
    }

    @Override
    public void send(
            Notification notification,
            String subject,
            String message
    ) {

        notificationRepository.save(
                notification
        );

    }

}