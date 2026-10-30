package com.pm.notificationservice.delivery.channel;

import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailChannel implements NotificationChannel {

    private static final Logger log =
            LoggerFactory.getLogger(EmailChannel.class);

    public EmailChannel() {
    }

    @Override
    public NotificationChannelType getChannelType() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public void send(
            Notification notification,
            String subject,
            String message
    ) {

        /*
         * Placeholder implementation.
         *
         * This will later integrate with an email provider
         * such as SendGrid, AWS SES, Mailgun or SMTP.
         */

        log.info(
                "Sending EMAIL notification. Subject={}",
                subject
        );

    }

}