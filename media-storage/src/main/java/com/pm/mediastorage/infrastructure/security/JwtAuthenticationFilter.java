package com.pm.mediastorage.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(
                    JwtAuthenticationFilter.class
            );

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil
    ) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException,
            IOException {

        String requestUri =
                request.getRequestURI();

        String authHeader =
                request.getHeader(
                        "Authorization"
                );

        log.debug(
                "Processing JWT authentication for {} {}",
                request.getMethod(),
                requestUri
        );

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            log.debug(
                    "No Bearer token found for request {}",
                    requestUri
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authHeader.substring(7);

        try {

            boolean valid =
                    jwtUtil.validateToken(token);

            if (!valid) {

                log.warn(
                        "Invalid JWT received for request {}",
                        requestUri
                );

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                return;
            }

            String tokenType =
                    jwtUtil.getTokenType(token);

            log.debug(
                    "JWT validated successfully. Token type: {}",
                    tokenType
            );

            if (!"access".equals(tokenType)) {

                log.warn(
                        "Non-access token used for authenticated request {}. Type: {}",
                        requestUri,
                        tokenType
                );

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            String userId =
                    jwtUtil.getUserIdFromToken(
                            token
                    );

            log.debug(
                    "JWT user ID resolved: {}",
                    userId
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.emptyList()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

            log.info(
                    "Authenticated user {} for {} {}",
                    userId,
                    request.getMethod(),
                    requestUri
            );

            filterChain.doFilter(
                    request,
                    response
            );

        } catch (Exception exception) {

            log.error(
                    "JWT authentication failed for request {} {}",
                    request.getMethod(),
                    requestUri,
                    exception
            );

            SecurityContextHolder.clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
        }
    }
}