package com.pm.bookingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.bookingservice.api.error.ApiError;
import com.pm.bookingservice.api.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;



@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<?> apiResponse = ApiResponse.failure(
                new ApiError(
                        "FORBIDDEN",
                        "You do not have permission",
                        Instant.now()
                )
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
