package com.pm.notificationservice.api.error;

import com.pm.notificationservice.api.error.response.ApiError;
import com.pm.notificationservice.api.error.response.ApiResponse;
import com.pm.notificationservice.domain.exception.NotificationAlreadyExistsException;
import com.pm.notificationservice.domain.exception.NotificationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotificationNotFound(

            NotificationNotFoundException ex

    ) {

        return buildResponse(

                HttpStatus.NOT_FOUND,

                "NOTIFICATION_NOT_FOUND",

                ex.getMessage()

        );

    }

    @ExceptionHandler(NotificationAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotificationAlreadyExists(

            NotificationAlreadyExistsException ex

    ) {

        return buildResponse(

                HttpStatus.CONFLICT,

                "NOTIFICATION_ALREADY_EXISTS",

                ex.getMessage()

        );

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(

            MethodArgumentNotValidException ex

    ) {

        String message = ex.getBindingResult()

                .getFieldErrors()

                .stream()

                .map(fieldError ->

                        fieldError.getField()

                                + ": "

                                + fieldError.getDefaultMessage()

                )

                .collect(Collectors.joining(", "));

        return buildResponse(

                HttpStatus.BAD_REQUEST,

                "VALIDATION_ERROR",

                message

        );

    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(

            IllegalArgumentException ex

    ) {

        return buildResponse(

                HttpStatus.BAD_REQUEST,

                "INVALID_ARGUMENT",

                ex.getMessage()

        );

    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(

            IllegalStateException ex

    ) {

        return buildResponse(

                HttpStatus.CONFLICT,

                "INVALID_STATE",

                ex.getMessage()

        );

    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(

            AccessDeniedException ex

    ) {

        return buildResponse(

                HttpStatus.FORBIDDEN,

                "ACCESS_DENIED",

                "You do not have permission to perform this action."

        );

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(

            Exception ex

    ) {

        return buildResponse(

                HttpStatus.INTERNAL_SERVER_ERROR,

                "INTERNAL_SERVER_ERROR",

                "An unexpected error occurred."

        );

    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(

            HttpStatus status,

            String code,

            String message

    ) {

        return ResponseEntity.status(status)

                .body(

                        ApiResponse.error(

                                new ApiError(

                                        code,

                                        message,

                                        Instant.now()

                                )

                        )

                );

    }

}