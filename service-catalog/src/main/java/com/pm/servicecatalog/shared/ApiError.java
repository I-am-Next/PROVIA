package com.pm.servicecatalog.shared;

import java.time.LocalDateTime;

public record ApiError(
        String code,
        String message,
        LocalDateTime timestamp) { }
