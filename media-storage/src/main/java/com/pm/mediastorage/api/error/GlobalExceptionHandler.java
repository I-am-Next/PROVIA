package com.pm.mediastorage.api.error;

import com.pm.mediastorage.api.dto.response.ApiResponse;
import com.pm.mediastorage.exception.InvalidMediaFileException;
import com.pm.mediastorage.exception.MediaAccessDeniedException;
import com.pm.mediastorage.exception.MediaNotFoundException;
import com.pm.mediastorage.exception.MediaTooLargeException;
import com.pm.mediastorage.exception.StorageException;
import com.pm.mediastorage.exception.UnsupportedMediaTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(InvalidMediaFileException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleInvalidMediaFile(
            InvalidMediaFileException exception
    ) {

        log.warn(
                "Invalid media file request: {}",
                exception.getMessage()
        );

        return buildFailure(
                HttpStatus.BAD_REQUEST,
                "INVALID_MEDIA_FILE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(UnsupportedMediaTypeException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleUnsupportedMediaType(
            UnsupportedMediaTypeException exception
    ) {

        log.warn(
                "Unsupported media type: {}",
                exception.getMessage()
        );

        return buildFailure(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MediaTooLargeException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleMediaTooLarge(
            MediaTooLargeException exception
    ) {

        log.warn(
                "Media file exceeds allowed size: {}",
                exception.getMessage()
        );

        return buildFailure(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "MEDIA_TOO_LARGE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MediaNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleMediaNotFound(
            MediaNotFoundException exception
    ) {

        log.warn(
                "Media not found: {}",
                exception.getMessage()
        );

        return buildFailure(
                HttpStatus.NOT_FOUND,
                "MEDIA_NOT_FOUND",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MediaAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleAccessDenied(
            MediaAccessDeniedException exception
    ) {

        log.warn(
                "Media access denied: {}",
                exception.getMessage()
        );

        return buildFailure(
                HttpStatus.FORBIDDEN,
                "MEDIA_ACCESS_DENIED",
                exception.getMessage()
        );
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleStorageException(
            StorageException exception
    ) {

        log.error(
                "Media storage operation failed",
                exception
        );

        return buildFailure(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "STORAGE_ERROR",
                "Media storage operation failed"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>>
    handleUnexpectedException(
            Exception exception
    ) {

        log.error(
                "Unexpected exception occurred "
                        + "while processing media request",
                exception
        );

        return buildFailure(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred"
        );
    }

    private ResponseEntity<ApiResponse<Void>>
    buildFailure(
            HttpStatus status,
            String code,
            String message
    ) {

        log.debug(
                "Returning error response. status={}, code={}, message={}",
                status,
                code,
                message
        );

        ApiError error =
                new ApiError(
                        code,
                        message,
                        Instant.now()
                );

        return ResponseEntity
                .status(status)
                .body(
                        ApiResponse.failure(error)
                );
    }
}