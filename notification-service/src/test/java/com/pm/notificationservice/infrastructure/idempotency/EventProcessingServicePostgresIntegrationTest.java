package com.pm.notificationservice.infrastructure.idempotency;

import com.pm.contracts.common.DomainEvent;
import com.pm.notificationservice.application.service.impl.EventProcessingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
class EventProcessingServicePostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:16-alpine"
            )
                    .withDatabaseName("notification_test")
                    .withUsername("notification_test")
                    .withPassword("notification_test");

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                POSTGRES::getDriverClassName
        );

        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "create-drop"
        );

        registry.add(
                "spring.flyway.enabled",
                () -> "false"
        );
    }

    @Autowired
    private EventProcessingServiceImpl eventProcessingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {

        jdbcTemplate.execute(
                """
                CREATE TABLE IF NOT EXISTS test_processed_effects (
                    event_id UUID PRIMARY KEY
                )
                """
        );

        jdbcTemplate.update(
                "DELETE FROM test_processed_effects"
        );
    }

    @Test
    void concurrentProcessingOfSameEvent_isProcessedOnlyOnce()
            throws Exception {

        UUID eventId =
                UUID.randomUUID();

        DomainEvent event =
                mock(DomainEvent.class);

        when(event.eventId())
                .thenReturn(eventId);

        when(event.eventType())
                .thenReturn("TEST_EVENT");

        CountDownLatch startLatch =
                new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {

            Future<?> first =
                    executor.submit(
                            () -> processEvent(
                                    event,
                                    startLatch
                            )
                    );

            Future<?> second =
                    executor.submit(
                            () -> processEvent(
                                    event,
                                    startLatch
                            )
                    );

            /*
             * Release both threads at approximately
             * the same time so that they race against
             * the same PostgreSQL database state.
             */
            startLatch.countDown();

            first.get(
                    30,
                    TimeUnit.SECONDS
            );

            second.get(
                    30,
                    TimeUnit.SECONDS
            );

        } finally {

            executor.shutdownNow();
        }

        Integer effectCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM test_processed_effects
                        WHERE event_id = ?
                        """,
                        Integer.class,
                        eventId
                );

        assertThat(effectCount)
                .isEqualTo(1);

        Integer processedEventCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM processed_events
                        WHERE event_id = ?
                        """,
                        Integer.class,
                        eventId
                );

        assertThat(processedEventCount)
                .isEqualTo(1);
    }

    private void processEvent(
            DomainEvent event,
            CountDownLatch startLatch
    ) {
        try {

            startLatch.await(
                    10,
                    TimeUnit.SECONDS
            );

            eventProcessingService.process(
                    event,
                    () -> insertTestEffect(
                            event.eventId()
                    )
            );

        } catch (Exception exception) {

            /*
             * One concurrent transaction may lose the
             * processed_events primary-key race.
             *
             * The important assertion is the final
             * database state, not that both threads
             * complete without an exception.
             */
        }
    }

    private void insertTestEffect(
            UUID eventId
    ) {

        jdbcTemplate.update(
                """
                INSERT INTO test_processed_effects (
                    event_id
                )
                VALUES (?)
                """,
                eventId
        );
    }
}