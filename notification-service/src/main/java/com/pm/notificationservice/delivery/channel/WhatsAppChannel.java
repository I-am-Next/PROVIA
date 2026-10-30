package com.pm.notificationservice.delivery.channel;

import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.NotificationChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppChannel implements NotificationChannel {

    private static final Logger log =
            LoggerFactory.getLogger(WhatsAppChannel.class);

    public WhatsAppChannel() {
    }

    @Override
    public NotificationChannelType getChannelType() {
        return NotificationChannelType.WHATSAPP;
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
         * This will later integrate with the
         * WhatsApp Business API through providers
         * such as Meta, Twilio or 360dialog.
         */

        log.info(
                "Sending WHATSAPP notification."
        );

    }

}