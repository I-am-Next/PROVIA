package com.pm.ratingservice.api.error;

import com.pm.ratingservice.domain.exception.BookingNotCompletedException;
import com.pm.ratingservice.domain.exception.BookingNotFoundException;
import com.pm.ratingservice.domain.exception.DuplicateRatingException;
import com.pm.ratingservice.domain.exception.RatingNotFoundException;
import com.pm.ratingservice.domain.exception.UnauthorizedRatingDeleteException;
import com.pm.ratingservice.domain.exception.UnauthorizedRatingUpdateException;
import com.pm.ratingservice.domain.exception.UserNotBookingParticipantException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/v1/ratings");
    }

    @Test
    void shouldReturn404ForRatingNotFoundException() {

        UUID ratingId = UUID.randomUUID();

        RatingNotFoundException exception =
                new RatingNotFoundException(ratingId);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleRatingNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                404,
                response.getBody().status()
        );

        assertEquals(
                "RATING_NOT_FOUND",
                response.getBody().error()
        );

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/ratings",
                response.getBody().path()
        );

        assertNotNull(
                response.getBody().timestamp()
        );
    }

    @Test
    void shouldReturn404ForBookingNotFoundException() {

        UUID bookingId = UUID.randomUUID();

        BookingNotFoundException exception =
                new BookingNotFoundException(bookingId);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleBookingNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                404,
                response.getBody().status()
        );

        assertEquals(
                "BOOKING_NOT_FOUND",
                response.getBody().error()
        );

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/ratings",
                response.getBody().path()
        );

        assertNotNull(
                response.getBody().timestamp()
        );
    }

    @Test
    void shouldReturn409ForDuplicateRatingException() {

        DuplicateRatingException exception =
                new DuplicateRatingException();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleDuplicateRating(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                409,
                response.getBody().status()
        );

        assertEquals(
                "DUPLICATE_RATING",
                response.getBody().error()
        );

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/ratings",
                response.getBody().path()
        );

        assertNotNull(
                response.getBody().timestamp()
        );
    }

    @Test
    void shouldReturn409ForBookingNotCompletedException() {

        BookingNotCompletedException exception =
                new BookingNotCompletedException();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleBookingNotCompleted(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                409,
                response.getBody().status()
        );

        assertEquals(
                "BOOKING_NOT_COMPLETED",
                response.getBody().error()
        );

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/ratings",
                response.getBody().path()
        );

        assertNotNull(
                response.getBody().timestamp()
        );
    }

    @Test
    void shouldReturn403ForUserNotBookingParticipantException() {

        UserNotBookingParticipantException exception =
                new UserNotBookingParticipantException();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleUserNotBookingParticipant(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                403,
                response.getBody().status()
        );

        assertEquals(
                "USER_NOT_BOOKING_PARTICIPANT",
                response.getBody().error()
        );

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/ratings",
                response.getBody().path()
        );

        assertNotNull(
                response.getBody().timestamp()
        );
    }

    @Test
    void shouldReturn403ForUnauthorizedRatingUpdateException() {

        UnauthorizedRatingUpdateException exception =
                new UnauthorizedRatingUpdateException();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnauthorizedRatingUpdate(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                403,
                response.getBody().status()
        );

        assertEquals(
                "UNAUTHORIZED_RATING_UPDATE",
                response.getBody().error()
        );

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/ratings",
                response.getBody().path()
        );

        assertNotNull(
                response.getBody().timestamp()
        );
    }

    @Test
    void shouldReturn403ForUnauthorizedRatingDeleteException() {

        UnauthorizedRatingDeleteException exception =
                new UnauthorizedRatingDeleteException();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnauthorizedRatingDelete(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                403,
                response.getBody().status()
        );

        assertEquals(
                "UNAUTHORIZED_RATING_DELETE",
                response.getBody().error()
        );

        assertEquals(
                exception.getMessage(),
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/ratings",
                response.getBody().path()
        );

        assertNotNull(
                response.getBody().timestamp()
        );
    }

    @Test
    void shouldReturn500AndHideInternalExceptionMessage() {

        String internalMessage =
                "Database connection failed: password=secret";

        Exception exception =
                new RuntimeException(internalMessage);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleGenericException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                500,
                response.getBody().status()
        );

        assertEquals(
                "INTERNAL_SERVER_ERROR",
                response.getBody().error()
        );

        assertEquals(
                "An unexpected error occurred.",
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/ratings",
                response.getBody().path()
        );

        assertNotNull(
                response.getBody().timestamp()
        );

        // Most important security assertion:
        // the internal exception message must not reach the client.
        org.junit.jupiter.api.Assertions.assertNotEquals(
                internalMessage,
                response.getBody().message()
        );
    }
}