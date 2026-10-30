package com.pm.providerservice.api;

import com.pm.providerservice.api.error.ApiErrorResponse;
import com.pm.providerservice.api.error.GlobalExceptionHandler;
import com.pm.providerservice.exception.CatalogLookupException;
import com.pm.providerservice.exception.DuplicateProviderServiceException;
import com.pm.providerservice.exception.ProviderProfileNotFoundException;
import com.pm.providerservice.exception.ProviderServiceNotFoundException;
import com.pm.providerservice.exception.ProviderServiceOwnershipException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        when(request.getRequestURI())
                .thenReturn("/providers/test");
    }

    @Test
    void shouldReturn404ForProviderProfileNotFound() {

        ResponseEntity<ApiErrorResponse> response =
                handler.handleApiException(
                        new ProviderProfileNotFoundException(),
                        request
                );

        assertEquals(
                404,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "PROVIDER_PROFILE_NOT_FOUND",
                response.getBody().getError()
        );
    }

    @Test
    void shouldReturn404ForProviderServiceNotFound() {

        ResponseEntity<ApiErrorResponse> response =
                handler.handleApiException(
                        new ProviderServiceNotFoundException(
                                java.util.UUID.randomUUID()
                        ),
                        request
                );

        assertEquals(
                404,
                response.getStatusCode().value()
        );

        assertEquals(
                "PROVIDER_SERVICE_NOT_FOUND",
                response.getBody().getError()
        );
    }

    @Test
    void shouldReturn403ForOwnershipViolation() {

        ResponseEntity<ApiErrorResponse> response =
                handler.handleApiException(
                        new ProviderServiceOwnershipException(),
                        request
                );

        assertEquals(
                403,
                response.getStatusCode().value()
        );

        assertEquals(
                "PROVIDER_SERVICE_FORBIDDEN",
                response.getBody().getError()
        );
    }

    @Test
    void shouldReturn409ForDuplicateProviderService() {

        ResponseEntity<ApiErrorResponse> response =
                handler.handleApiException(
                        new DuplicateProviderServiceException(),
                        request
                );

        assertEquals(
                409,
                response.getStatusCode().value()
        );

        assertEquals(
                "DUPLICATE_PROVIDER_SERVICE",
                response.getBody().getError()
        );
    }

    @Test
    void shouldReturn502ForCatalogFailure() {

        ResponseEntity<ApiErrorResponse> response =
                handler.handleApiException(
                        new CatalogLookupException(
                                "Catalog unavailable"
                        ),
                        request
                );

        assertEquals(
                502,
                response.getStatusCode().value()
        );

        assertEquals(
                "CATALOG_LOOKUP_FAILED",
                response.getBody().getError()
        );
    }

    @Test
    void shouldHideUnexpectedExceptionDetails() {

        RuntimeException exception =
                new RuntimeException(
                        "database password leaked"
                );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleGenericException(
                        exception,
                        request
                );

        assertEquals(
                500,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "INTERNAL_SERVER_ERROR",
                response.getBody().getError()
        );

        assertEquals(
                "An unexpected error occurred.",
                response.getBody().getMessage()
        );

        assertFalse(
                response.getBody()
                        .getMessage()
                        .contains("database password")
        );
    }
}