package com.pm.paymentservicenew.api.error;

import com.pm.paymentservicenew.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        when(request.getRequestURI())
                .thenReturn("/api/v1/payments/test");
    }

    /*
     * ============================================================
     * 404 NOT FOUND
     * ============================================================
     */

    @Test
    void shouldReturn404ForPaymentNotFoundException() {

        PaymentNotFoundException exception =
                new PaymentNotFoundException(
                        UUID.randomUUID(),
                        "id"
                );

        ResponseEntity<ApiError> response =
                handler.handleNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/payments/test",
                response.getBody().path()
        );
    }

    @Test
    void shouldReturn404ForWalletNotFoundException() {

        WalletNotFoundException exception =
                new WalletNotFoundException(
                        UUID.randomUUID()
                );

        ResponseEntity<ApiError> response =
                handler.handleNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn404ForEscrowNotFoundException() {

        EscrowNotFoundException exception =
                new EscrowNotFoundException(
                        UUID.randomUUID(),
                        "payment"
                );

        ResponseEntity<ApiError> response =
                handler.handleNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn404ForWithdrawalNotFoundException() {

        WithdrawalNotFoundException exception =
                new WithdrawalNotFoundException(
                        UUID.randomUUID(),
                        "id"
                );

        ResponseEntity<ApiError> response =
                handler.handleNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    /*
     * ============================================================
     * 409 CONFLICT
     * ============================================================
     */

    @Test
    void shouldReturn409ForInvalidPaymentStateException() {

        InvalidPaymentStateException exception =
                new InvalidPaymentStateException(
                        "Payment cannot be completed."
                );

        ResponseEntity<ApiError> response =
                handler.handleConflict(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn409ForInvalidEscrowStateException() {

        InvalidEscrowStateException exception =
                new InvalidEscrowStateException(
                        "Escrow cannot be released."
                );

        ResponseEntity<ApiError> response =
                handler.handleConflict(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn409ForInvalidWithdrawalStateException() {

        InvalidWithdrawalStateException exception =
                new InvalidWithdrawalStateException(
                        "Withdrawal cannot be completed."
                );

        ResponseEntity<ApiError> response =
                handler.handleConflict(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn409ForEscrowOverReleaseException() {

        EscrowOverReleaseException exception =
                new EscrowOverReleaseException(
                        "Escrow release exceeds available balance."
                );

        ResponseEntity<ApiError> response =
                handler.handleConflict(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn409ForPaymentAlreadyExistsException() {

        UUID bookingId = UUID.randomUUID();

        PaymentAlreadyExistsException exception =
                new PaymentAlreadyExistsException(
                        bookingId
                );

        ResponseEntity<ApiError> response =
                handler.handleConflict(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    /*
     * ============================================================
     * 400 BAD REQUEST
     * ============================================================
     */

    @Test
    void shouldReturn400ForInvalidAmountException() {

        InvalidAmountException exception =
                new InvalidAmountException();

        ResponseEntity<ApiError> response =
                handler.handleBadRequest(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn400ForInsufficientWalletBalanceException() {

        InsufficientWalletBalanceException exception =
                new InsufficientWalletBalanceException(
                        "Insufficient wallet balance."
                );

        ResponseEntity<ApiError> response =
                handler.handleBadRequest(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn400ForInvalidWebhookPayloadException() {

        InvalidWebhookPayloadException exception =
                new InvalidWebhookPayloadException();

        ResponseEntity<ApiError> response =
                handler.handleBadRequest(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn400ForPaymentVerificationFailedException() {

        PaymentVerificationFailedException exception =
                new PaymentVerificationFailedException();

        ResponseEntity<ApiError> response =
                handler.handleBadRequest(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn400ForPaymentNotSuccessfulException() {

        PaymentNotSuccessfulException exception =
                new PaymentNotSuccessfulException();

        ResponseEntity<ApiError> response =
                handler.handleBadRequest(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    /*
     * ============================================================
     * 401 UNAUTHORIZED
     * ============================================================
     */

    @Test
    void shouldReturn401ForInvalidWebhookSignatureException() {

        InvalidWebhookSignatureException exception =
                new InvalidWebhookSignatureException();

        ResponseEntity<ApiError> response =
                handler.handleUnauthorized(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn401ForUserNotAuthenticatedException() {

        UserNotAuthenticatedException exception =
                new UserNotAuthenticatedException();

        ResponseEntity<ApiError> response =
                handler.handleUnauthorized(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    /*
     * ============================================================
     * 403 FORBIDDEN
     * ============================================================
     */

    @Test
    void shouldReturn403ForPaymentAccessDeniedException() {

        PaymentAccessDeniedException exception =
                new PaymentAccessDeniedException(
                        "You do not have access to this payment."
                );

        ResponseEntity<ApiError> response =
                handler.handleForbidden(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn403ForWalletAccessDeniedException() {

        WalletAccessDeniedException exception =
                new WalletAccessDeniedException(
                        "You do not have access to this wallet."
                );

        ResponseEntity<ApiError> response =
                handler.handleForbidden(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    @Test
    void shouldReturn403ForInvalidRoleException() {

        InvalidRoleException exception =
                new InvalidRoleException(
                        "Provider role required."
                );

        ResponseEntity<ApiError> response =
                handler.handleForbidden(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );
    }

    /*
     * ============================================================
     * 500 INTERNAL SERVER ERROR
     * ============================================================
     */

    @Test
    void shouldReturn500ForUnexpectedExceptionWithoutExposingInternalDetails() {

        when(request.getMethod())
                .thenReturn("GET");

        Exception exception =
                new RuntimeException(
                        "SQL grammar error: password=super-secret"
                );

        ResponseEntity<ApiError> response =
                handler.handleGeneric(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "An unexpected error occurred.",
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/payments/test",
                response.getBody().path()
        );

        /*
         * The internal exception message must not
         * be exposed to the API client.
         */
        assertEquals(
                false,
                response.getBody()
                        .message()
                        .contains("SQL grammar error")
        );

        assertEquals(
                false,
                response.getBody()
                        .message()
                        .contains("super-secret")
        );
    }
}