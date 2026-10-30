package com.pm.ratingservice.api.error;

import com.pm.ratingservice.domain.exception.BookingNotCompletedException;
import com.pm.ratingservice.domain.exception.BookingNotFoundException;
import com.pm.ratingservice.domain.exception.DuplicateRatingException;
import com.pm.ratingservice.domain.exception.RatingNotFoundException;
import com.pm.ratingservice.domain.exception.UnauthorizedRatingDeleteException;
import com.pm.ratingservice.domain.exception.UnauthorizedRatingUpdateException;
import com.pm.ratingservice.domain.exception.UserNotBookingParticipantException;
import jakarta.servlet.http.HttpServletRequest;
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
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =========================================================
    // RATING NOT FOUND
    // =========================================================

    @ExceptionHandler(RatingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRatingNotFound(
            RatingNotFoundException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "RATING_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // =========================================================
    // BOOKING NOT FOUND
    // =========================================================

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingNotFound(
            BookingNotFoundException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "BOOKING_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // =========================================================
    // DUPLICATE RATING
    // =========================================================

    @ExceptionHandler(DuplicateRatingException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateRating(
            DuplicateRatingException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "DUPLICATE_RATING",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // =========================================================
    // BOOKING NOT COMPLETED
    // =========================================================

    @ExceptionHandler(BookingNotCompletedException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingNotCompleted(
            BookingNotCompletedException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "BOOKING_NOT_COMPLETED",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // =========================================================
    // USER NOT BOOKING PARTICIPANT
    // =========================================================

    @ExceptionHandler(UserNotBookingParticipantException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotBookingParticipant(
            UserNotBookingParticipantException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "USER_NOT_BOOKING_PARTICIPANT",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // =========================================================
    // UNAUTHORIZED RATING UPDATE
    // =========================================================

    @ExceptionHandler(UnauthorizedRatingUpdateException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedRatingUpdate(
            UnauthorizedRatingUpdateException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "UNAUTHORIZED_RATING_UPDATE",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // =========================================================
    // UNAUTHORIZED RATING DELETE
    // =========================================================

    @ExceptionHandler(UnauthorizedRatingDeleteException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedRatingDelete(
            UnauthorizedRatingDeleteException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "UNAUTHORIZED_RATING_DELETE",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // =========================================================
    // GENERIC UNEXPECTED ERROR
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {

        /*
         * Log the complete exception server-side so developers and
         * operators still have the information required for diagnosis.
         *
         * Do NOT expose ex.getMessage() to the client.
         */
        log.error(
                "Unexpected exception while processing rating request {}",
                request.getRequestURI(),
                ex
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred.",
                request.getRequestURI()
        );
    }

    // =========================================================
    // RESPONSE BUILDER
    // =========================================================

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            String path
    ) {

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                path
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}