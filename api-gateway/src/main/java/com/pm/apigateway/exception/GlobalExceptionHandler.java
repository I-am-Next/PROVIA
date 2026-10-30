package com.pm.apigateway.exception;

import com.pm.apigateway.error.ApiResponse;
import com.pm.apigateway.util.ApiResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler implements WebExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);



    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 🔥 LOG THE REAL EXCEPTION
        log.error("Gateway error caught", ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String code = "INTERNAL_ERROR";
        String message = "Something went wrong";

        // ---- Handle known exception ----

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : message;
            code = "REQUEST_ERROR";
        }

        else if (ex instanceof ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            code = "SERVICE_UNAVAILABLE";
            message = "Service is currently unavailable";
        }

        else if (ex instanceof TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            code = "TIMEOUT";
            message = "Request timed out";
        }

        else if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            code = "FORBIDDEN";
            message = "You do not have permission";
        }

        // ---- Build standardized response ----
        ApiResponse<Object> apiResponse = ApiResponse.error(
                code,
                message
        );

        exchange.getResponse().setStatusCode(status);

        return ApiResponseWriter.write(exchange.getResponse(), apiResponse);
    }
}
