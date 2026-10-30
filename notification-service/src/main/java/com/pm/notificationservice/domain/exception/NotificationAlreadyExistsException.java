package com.pm.notificationservice.domain.exception;

import java.util.UUID;

public class NotificationAlreadyExistsException extends RuntimeException {

    public NotificationAlreadyExistsException(
            UUID notificationId
    ) {

        super(String.format(
                "Notification '%s' has already been marked as read.",
                notificationId
        ));

    }

}