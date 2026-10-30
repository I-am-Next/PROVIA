package com.pm.authservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class RedisRefreshTokenServiceIntegrationTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @Test
    void concurrentRotationShouldAllowExactlyOneRequest() throws Exception {
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(
                        REDIS.getHost(),
                        REDIS.getMappedPort(6379)
                );

        LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(configuration);

        connectionFactory.afterPropertiesSet();

        try {
            StringRedisTemplate template =
                    new StringRedisTemplate(connectionFactory);
            template.afterPropertiesSet();

            RedisRefreshTokenService service =
                    new RedisRefreshTokenService(template);

            UUID userId = UUID.randomUUID();
            String oldToken = "old-refresh-token";
            String replacementA = "replacement-token-a";
            String replacementB = "replacement-token-b";
            long ttlMillis = 60_000L;

            service.store(oldToken, userId, ttlMillis);

            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<Boolean> attemptA = executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    return service.rotate(
                            oldToken,
                            replacementA,
                            userId,
                            ttlMillis
                    );
                });

                Future<Boolean> attemptB = executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    return service.rotate(
                            oldToken,
                            replacementB,
                            userId,
                            ttlMillis
                    );
                });

                // Wait until both workers are ready before releasing them.
                ready.await();
                start.countDown();

                boolean resultA = attemptA.get();
                boolean resultB = attemptB.get();

                assertNotEquals(
                        resultA,
                        resultB,
                        "Exactly one concurrent rotation must succeed"
                );

                assertNull(
                        service.getUserId(oldToken),
                        "The old refresh token must no longer be active"
                );

                String winningToken = resultA
                        ? replacementA
                        : replacementB;

                String losingToken = resultA
                        ? replacementB
                        : replacementA;

                assertEquals(
                        userId.toString(),
                        service.getUserId(winningToken),
                        "The winning replacement token must be stored"
                );

                assertNull(
                        service.getUserId(losingToken),
                        "The losing replacement token must not be stored"
                );

                // Confirm the user's Redis set contains only the winner.
                var activeTokens = template.opsForSet()
                        .members("user:" + userId + ":tokens");

                assertTrue(activeTokens != null);
                assertEquals(1, activeTokens.size());
                assertTrue(activeTokens.contains(winningToken));
                assertFalse(activeTokens.contains(losingToken));

            } finally {
                executor.shutdownNow();
            }

        } finally {
            connectionFactory.destroy();
        }
    }
}