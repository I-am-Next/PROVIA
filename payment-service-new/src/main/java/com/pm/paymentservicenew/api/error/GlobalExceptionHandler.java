package com.pm.paymentservicenew.api.error;

import com.pm.paymentservicenew.domain.exception.*;
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

    /*
     * =========================
     * 404 NOT FOUND
     * =========================
     */

    @ExceptionHandler({
            PaymentNotFoundException.class,
            WalletNotFoundException.class,
            EscrowNotFoundException.class,
            WithdrawalNotFoundException.class
    })
    public ResponseEntity<ApiError> handleNotFound(
            RuntimeException ex,
            HttpServletRequest request
    ) {

        return buildSafeError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    /*
     * =========================
     * 409 CONFLICT
     * =========================
     */

    @ExceptionHandler({
            InvalidPaymentStateException.class,
            InvalidEscrowStateException.class,
            InvalidWithdrawalStateException.class,
            EscrowOverReleaseException.class,
            PaymentAlreadyExistsException.class
    })
    public ResponseEntity<ApiError> handleConflict(
            RuntimeException ex,
            HttpServletRequest request
    ) {

        return buildSafeError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    /*
     * =========================
     * 400 BAD REQUEST
     * =========================
     */

    @ExceptionHandler({
            InvalidAmountException.class,
            InsufficientWalletBalanceException.class,
            InvalidWebhookPayloadException.class,
            PaymentVerificationFailedException.class,
            PaymentNotSuccessfulException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(
            RuntimeException ex,
            HttpServletRequest request
    ) {

        return buildSafeError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    /*
     * =========================
     * 401 UNAUTHORIZED
     * =========================
     */

    @ExceptionHandler({
            InvalidWebhookSignatureException.class,
            UserNotAuthenticatedException.class
    })
    public ResponseEntity<ApiError> handleUnauthorized(
            RuntimeException ex,
            HttpServletRequest request
    ) {

        return buildSafeError(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request
        );
    }

    /*
     * =========================
     * 403 FORBIDDEN
     * =========================
     */

    @ExceptionHandler({
            PaymentAccessDeniedException.class,
            WalletAccessDeniedException.class,
            InvalidRoleException.class
    })
    public ResponseEntity<ApiError> handleForbidden(
            RuntimeException ex,
            HttpServletRequest request
    ) {

        return buildSafeError(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request
        );
    }

    /*
     * =========================
     * 500 INTERNAL SERVER ERROR
     * =========================
     *
     * Unexpected exceptions must never expose
     * their internal message to the client.
     *
     * The complete exception is logged server-side.
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error(
                "Unhandled exception while processing request. " +
                        "method={}, path={}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        return buildSafeError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                request
        );
    }

    /*
     * =========================
     * Helper
     * =========================
     */

    private ResponseEntity<ApiError> buildSafeError(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {

        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}