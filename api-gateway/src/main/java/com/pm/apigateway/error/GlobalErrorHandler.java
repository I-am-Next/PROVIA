package com.pm.apigateway.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(-2) // Run before Spring's default handler
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalErrorHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            Throwable ex
    ) {

        // Log the real exception
        log.error("Unhandled gateway error", ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Something went wrong";
        String code = "GATEWAY_ERROR";

        // Downstream ResponseStatusException
        if (ex instanceof org.springframework.web.server.ResponseStatusException rse) {

            status = HttpStatus.valueOf(rse.getStatusCode().value());

            message = rse.getReason() != null
                    ? rse.getReason()
                    : message;

            code = "DOWNSTREAM_ERROR";
        }

        // Downstream service unavailable
        if (ex instanceof java.net.ConnectException) {

            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service unavailable";
            code = "SERVICE_UNAVAILABLE";
        }

        ApiResponse<Object> response =
                ApiResponse.error(code, message);

        byte[] bytes;

        try {

            bytes = objectMapper.writeValueAsBytes(response);

        } catch (Exception e) {

            log.error("Failed to serialize error response", e);

            bytes = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "SERIALIZATION_ERROR",
                "message": "%s"
              }
            }
            """
                    .formatted(ex.getMessage())
                    .getBytes(StandardCharsets.UTF_8);

        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        var buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

}