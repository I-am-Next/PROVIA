package com.pm.apigateway.error;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error
) {
    public static <T> ApiResponse<T> error(
            String code,
            String message
    ) {
        return new ApiResponse<>(
                false,
                null,
                new ApiError(
                        code,
                        message,
                        Instant.now()
                )
        );
    }
}