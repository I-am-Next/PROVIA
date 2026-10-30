package com.pm.notificationservice.api.error.response;

import java.time.Instant;

public record ApiError(

        String code,

        String message,

        Instant timestamp

) {
}
