package com.pm.providerservice.api.error;

import com.pm.providerservice.exception.ApiException;
import com.pm.providerservice.exception.CatalogLookupException;
import com.pm.providerservice.exception.DuplicateProviderServiceException;
import com.pm.providerservice.exception.ProviderProfileNotFoundException;
import com.pm.providerservice.exception.ProviderServiceNotFoundException;
import com.pm.providerservice.exception.ProviderServiceOwnershipException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

      private static final Logger log =
              LoggerFactory.getLogger(GlobalExceptionHandler.class);

      @ExceptionHandler(MethodArgumentNotValidException.class)
      public ResponseEntity<ApiErrorResponse> handleValidationException(
              MethodArgumentNotValidException ex,
              HttpServletRequest request
      ) {

            String message = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            ApiErrorResponse error = new ApiErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "VALIDATION_FAILED",
                    message,
                    request.getRequestURI()
            );

            return ResponseEntity
                    .badRequest()
                    .body(error);
      }

      @ExceptionHandler(ApiException.class)
      public ResponseEntity<ApiErrorResponse> handleApiException(
              ApiException ex,
              HttpServletRequest request
      ) {

            HttpStatus status = HttpStatus.BAD_REQUEST;

            if (ex instanceof ProviderProfileNotFoundException
                    || ex instanceof ProviderServiceNotFoundException) {

                  status = HttpStatus.NOT_FOUND;

            } else if (ex instanceof ProviderServiceOwnershipException) {

                  status = HttpStatus.FORBIDDEN;

            } else if (ex instanceof CatalogLookupException) {

                  status = HttpStatus.BAD_GATEWAY;

            } else if (ex instanceof DuplicateProviderServiceException) {

                  status = HttpStatus.CONFLICT;
            }

            ApiErrorResponse error = new ApiErrorResponse(
                    status.value(),
                    ex.getCode(),
                    ex.getMessage(),
                    request.getRequestURI()
            );

            return ResponseEntity
                    .status(status)
                    .body(error);
      }

      @ExceptionHandler(Exception.class)
      public ResponseEntity<ApiErrorResponse> handleGenericException(
              Exception ex,
              HttpServletRequest request
      ) {

            log.error(
                    "Unhandled exception processing request {}",
                    request.getRequestURI(),
                    ex
            );

            ApiErrorResponse error = new ApiErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "INTERNAL_SERVER_ERROR",
                    "An unexpected error occurred.",
                    request.getRequestURI()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
      }
}