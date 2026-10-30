package com.pm.notificationservice.domain.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(

            UUID notificationId

    ) {

        super(

                String.format(

                        "Notification with id '%s' was not found.",

                        notificationId

                )

        );

    }

    public NotificationNotFoundException(

            UUID id,

            String identifier

    ) {

        super(

                String.format(

                        "Notification with %s '%s' was not found.",

                        identifier,

                        id

                )

        );

    }

}