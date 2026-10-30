package com.pm.apigateway.filter;

import com.pm.apigateway.service.RedisRateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class UnifiedGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log =
            LoggerFactory.getLogger(UnifiedGatewayFilter.class);

    public static final String CORRELATION_HEADER = "X-Correlation-ID";

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private static final int DEFAULT_RATE_LIMIT = 60;
    private static final Duration DEFAULT_RATE_WINDOW =
            Duration.ofMinutes(1);

    private static final int LOGIN_RATE_LIMIT = 10;
    private static final Duration LOGIN_RATE_WINDOW =
            Duration.ofMinutes(1);

    private static final int REGISTER_RATE_LIMIT = 10;
    private static final Duration REGISTER_RATE_WINDOW =
            Duration.ofMinutes(1);

    private static final int REFRESH_RATE_LIMIT = 30;
    private static final Duration REFRESH_RATE_WINDOW =
            Duration.ofMinutes(1);

    private final RedisRateLimiterService rateLimiter;

    public UnifiedGatewayFilter(
            RedisRateLimiterService rateLimiter
    ) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        long startTime = System.currentTimeMillis();

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        String method = exchange.getRequest()
                .getMethod()
                .name();

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_HEADER);

        if (correlationId == null
                || correlationId.isBlank()
                || correlationId.length() > 128) {
            correlationId = UUID.randomUUID().toString();
        }

        final String requestCorrelationId = correlationId;

        log.info(
                "[{}] Incoming request: {} {}",
                requestCorrelationId,
                method,
                path
        );

        return exchange.getPrincipal()
                .ofType(JwtAuthenticationToken.class)
                .map(authentication -> {
                    String subject =
                            authentication.getToken().getSubject();

                    String role =
                            authentication.getToken()
                                    .getClaimAsString("role");

                    if (subject == null || subject.isBlank()) {
                        throw new IllegalStateException(
                                "Authenticated JWT has no subject"
                        );
                    }

                    if (role == null || role.isBlank()) {
                        role = "USER";
                    }

                    return new RequestIdentity(
                            subject,
                            role,
                            true
                    );
                })
                .defaultIfEmpty(
                        new RequestIdentity(
                                resolveRemoteAddress(exchange),
                                "ANONYMOUS",
                                false
                        )
                )
                .flatMap(identity -> {
                    RateLimitConfig config = getRateLimit(path);

                    String rateLimitKey = buildRateLimitKey(
                            identity.userId(),
                            path
                    );

                    /*
                     * Redis is called reactively. Do not block or call
                     * block() on the WebFlux event-loop thread.
                     */
                    return rateLimiter.isAllowed(
                                    rateLimitKey,
                                    config.maxRequests(),
                                    config.window()
                            )
                            .flatMap(allowed -> {
                                if (!allowed) {
                                    log.warn(
                                            "[{}] Rate limit exceeded: method={}, path={}, limit={}/{}",
                                            requestCorrelationId,
                                            method,
                                            path,
                                            config.maxRequests(),
                                            config.window()
                                    );

                                    return rateLimitExceeded(
                                            exchange,
                                            requestCorrelationId
                                    );
                                }

                                ServerWebExchange mutatedExchange =
                                        prepareExchange(
                                                exchange,
                                                identity,
                                                requestCorrelationId
                                        );

                                return chain.filter(mutatedExchange);
                            });
                })
                .doFinally(signalType -> {
                    long duration =
                            System.currentTimeMillis() - startTime;

                    HttpStatus status =
                            exchange.getResponse().getStatusCode()
                                    instanceof HttpStatus httpStatus
                                    ? httpStatus
                                    : null;

                    log.info(
                            "[{}] Completed {} {} -> {} ({} ms)",
                            requestCorrelationId,
                            method,
                            path,
                            status != null
                                    ? status.value()
                                    : "unknown",
                            duration
                    );
                });
    }

    private ServerWebExchange prepareExchange(
            ServerWebExchange exchange,
            RequestIdentity identity,
            String correlationId
    ) {
        return exchange.mutate()
                .request(request -> request.headers(headers -> {
                    /*
                     * Remove client-supplied identity headers first.
                     * Never propagate them as trusted identity.
                     */
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USER_ROLE_HEADER);

                    headers.set(
                            CORRELATION_HEADER,
                            correlationId
                    );

                    /*
                     * Add identity headers only when Spring Security
                     * has authenticated the request with a JWT.
                     */
                    if (identity.authenticated()) {
                        headers.set(
                                USER_ID_HEADER,
                                identity.userId()
                        );

                        headers.set(
                                USER_ROLE_HEADER,
                                identity.role()
                        );
                    }
                }))
                .build();
    }

    private RateLimitConfig getRateLimit(String path) {
        if (path.equals("/api/v1/auth/login")) {
            return new RateLimitConfig(
                    LOGIN_RATE_LIMIT,
                    LOGIN_RATE_WINDOW
            );
        }

        if (path.equals("/api/v1/auth/register")) {
            return new RateLimitConfig(
                    REGISTER_RATE_LIMIT,
                    REGISTER_RATE_WINDOW
            );
        }

        if (path.equals("/api/v1/auth/refresh")) {
            return new RateLimitConfig(
                    REFRESH_RATE_LIMIT,
                    REFRESH_RATE_WINDOW
            );
        }

        return new RateLimitConfig(
                DEFAULT_RATE_LIMIT,
                DEFAULT_RATE_WINDOW
        );
    }

    private String buildRateLimitKey(
            String identity,
            String path
    ) {
        /*
         * Keep authentication traffic separate from normal API traffic.
         * Authenticated requests use the JWT subject.
         * Anonymous requests use the remote IP address.
         */
        if (path.startsWith("/api/v1/auth/")) {
            return identity + ":auth";
        }

        return identity + ":api";
    }

    private String resolveRemoteAddress(
            ServerWebExchange exchange
    ) {
        InetSocketAddress remoteAddress =
                exchange.getRequest().getRemoteAddress();

        if (remoteAddress == null) {
            return "unknown";
        }

        if (remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return remoteAddress.getHostString();
    }

    private Mono<Void> rateLimitExceeded(
            ServerWebExchange exchange,
            String correlationId
    ) {
        exchange.getResponse().setStatusCode(
                HttpStatus.TOO_MANY_REQUESTS
        );

        exchange.getResponse().getHeaders().set(
                HttpHeaders.CONTENT_TYPE,
                "application/json"
        );

        exchange.getResponse().getHeaders().set(
                CORRELATION_HEADER,
                correlationId
        );

        String body = """
                {
                  "success": false,
                  "data": null,
                  "error": {
                    "code": "RATE_LIMIT_EXCEEDED",
                    "message": "Too many requests. Try again later.",
                    "timestamp": "%s"
                  }
                }
                """.formatted(Instant.now());

        var buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse()
                .writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -5;
    }

    private record RequestIdentity(
            String userId,
            String role,
            boolean authenticated
    ) {
    }

    private record RateLimitConfig(
            int maxRequests,
            Duration window
    ) {
    }
}