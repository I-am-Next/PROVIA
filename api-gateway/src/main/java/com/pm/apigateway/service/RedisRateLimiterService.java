package com.pm.apigateway.service;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;

@Service
public class RedisRateLimiterService {

    private static final int DEFAULT_MAX_REQUESTS = 60;

    private static final Duration DEFAULT_WINDOW =
            Duration.ofMinutes(1);

    /*
     * Atomically increments the request counter and ensures
     * the counter has an expiry.
     *
     * A new counter receives the configured expiry.
     * An existing counter without an expiry is repaired.
     * An existing counter with an expiry keeps its current window.
     */
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local count = redis.call('INCR', KEYS[1])
                    local ttl = redis.call('PTTL', KEYS[1])

                    if count == 1 or ttl < 0 then
                        redis.call('PEXPIRE', KEYS[1], ARGV[1])
                    end

                    return count
                    """,
                    Long.class
            );

    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisRateLimiterService(
            ReactiveStringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Checks the default application rate limit:
     * 60 requests per minute per key.
     */
    public Mono<Boolean> isAllowed(String key) {
        return isAllowed(
                key,
                DEFAULT_MAX_REQUESTS,
                DEFAULT_WINDOW
        );
    }

    /**
     * Checks a configurable rate limit using Redis.
     *
     * Redis increments and expiry management execute atomically
     * through the Lua script.
     */
    public Mono<Boolean> isAllowed(
            String key,
            int maxRequests,
            Duration window
    ) {
        if (key == null || key.isBlank()) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Rate-limit key must not be blank"
                    )
            );
        }

        if (maxRequests < 1) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Maximum requests must be greater than zero"
                    )
            );
        }

        if (window == null) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Rate-limit window must not be null"
                    )
            );
        }

        if (window.isZero() || window.isNegative()) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Rate-limit window must be positive"
                    )
            );
        }

        final long windowMillis;

        try {
            windowMillis = window.toMillis();
        } catch (ArithmeticException exception) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Rate-limit window is too large",
                            exception
                    )
            );
        }

        if (windowMillis < 1) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Rate-limit window must be at least one millisecond"
                    )
            );
        }

        String redisKey =
                "rate_limit:"
                        + key
                        + ":"
                        + maxRequests
                        + ":"
                        + windowMillis;

        return redisTemplate.execute(
                        RATE_LIMIT_SCRIPT,
                        Collections.singletonList(redisKey),
                        String.valueOf(windowMillis)
                )
                .next()
                .map(count -> count <= maxRequests)
                .switchIfEmpty(
                        Mono.error(
                                new IllegalStateException(
                                        "Redis rate limiter returned no result"
                                )
                        )
                );
    }
}