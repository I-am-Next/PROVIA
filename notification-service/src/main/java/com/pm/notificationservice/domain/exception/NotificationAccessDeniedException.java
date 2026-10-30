package com.pm.notificationservice.domain.exception;

public class NotificationAccessDeniedException extends RuntimeException {

    public NotificationAccessDeniedException() {

        super("You do not have permission to access this notification.");

    }

}