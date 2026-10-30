package com.pm.apigateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pm.apigateway.error.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

public class ApiResponseWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private ApiResponseWriter() {
        // Utility class; prevent instantiation.
    }

    public static Mono<Void> write(
            ServerHttpResponse response,
            ApiResponse<?> apiResponse
    ) {
        final byte[] bytes;

        try {
            bytes = MAPPER.writeValueAsBytes(apiResponse);
        } catch (Exception exception) {
            return Mono.error(exception);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes))
        );
    }
}