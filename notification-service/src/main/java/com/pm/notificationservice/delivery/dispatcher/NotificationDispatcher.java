package com.pm.notificationservice.delivery.dispatcher;

import com.pm.notificationservice.delivery.channel.NotificationChannel;
import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.NotificationChannelType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationDispatcher {

    private final Map<NotificationChannelType, NotificationChannel> channels;

    public NotificationDispatcher(
            List<NotificationChannel> notificationChannels
    ) {

        this.channels = new EnumMap<>(NotificationChannelType.class);

        for (NotificationChannel channel : notificationChannels) {

            this.channels.put(
                    channel.getChannelType(),
                    channel
            );

        }

    }

    public void dispatch(
            Notification notification,
            String subject,
            String message,
            List<NotificationChannelType> channelTypes
    ) {

        for (NotificationChannelType channelType : channelTypes) {

            NotificationChannel channel =
                    channels.get(channelType);

            if (channel != null) {

                channel.send(
                        notification,
                        subject,
                        message
                );

            }

        }

    }

}