package com.pm.notificationservice.delivery.channel;

import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmsChannel implements NotificationChannel {

    private static final Logger log =
            LoggerFactory.getLogger(SmsChannel.class);

    public SmsChannel() {
    }

    @Override
    public NotificationChannelType getChannelType() {
        return NotificationChannelType.SMS;
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
         * This will later integrate with an SMS provider
         * such as Twilio, Termii, Africa's Talking or Vonage.
         */

        log.info(
                "Sending SMS notification."
        );

    }

}