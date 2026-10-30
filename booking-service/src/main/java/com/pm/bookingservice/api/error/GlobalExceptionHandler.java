package com.pm.bookingservice.api.error;

import com.pm.bookingservice.api.response.ApiResponse;
import com.pm.bookingservice.domain.exception.BookingAccessDeniedException;
import com.pm.bookingservice.domain.exception.BookingNotFoundException;
import com.pm.bookingservice.domain.exception.InvalidBookingStateException;
import com.pm.bookingservice.domain.exception.UserNotAuthenticatedException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookingNotFound(
            BookingNotFoundException ex
    ) {

        return build(

                HttpStatus.NOT_FOUND,

                "BOOKING_NOT_FOUND",

                ex.getMessage()
        );
    }


    @ExceptionHandler(BookingAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
            BookingAccessDeniedException ex
    ) {

        return build(

                HttpStatus.FORBIDDEN,

                "ACCESS_DENIED",

                ex.getMessage()
        );
    }


    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UserNotAuthenticatedException ex
    ) {

        return build(

                HttpStatus.UNAUTHORIZED,

                "UNAUTHORIZED",

                ex.getMessage()
        );
    }


    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidState(
            InvalidBookingStateException ex
    ) {

        return build(

                HttpStatus.CONFLICT,

                "INVALID_BOOKING_STATE",

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

                .map(FieldError::getDefaultMessage)

                .collect(Collectors.joining(", "));


        return build(

                HttpStatus.BAD_REQUEST,

                "VALIDATION_ERROR",

                message
        );
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(
            ConstraintViolationException ex
    ) {

        return build(

                HttpStatus.BAD_REQUEST,

                "VALIDATION_ERROR",

                ex.getMessage()
        );
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(
            Exception ex
    ) {

        return build(

                HttpStatus.INTERNAL_SERVER_ERROR,

                "INTERNAL_ERROR",

                ex.getMessage()
        );
    }



    private ResponseEntity<ApiResponse<Void>> build(

            HttpStatus status,

            String code,

            String message
    ) {

        ApiError error = new ApiError(

                code,

                message,

                Instant.now()

        );


        return ResponseEntity

                .status(status)

                .body(

                        ApiResponse.failure(

                                error
                        )
                );

    }

}