package com.pm.notificationservice.infrastructure.outbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class OutboxClaimServicePostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("notification_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name",
                POSTGRES::getDriverClassName);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    private OutboxClaimService claimService;

    @Autowired
    private OutboxEventRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void concurrentPublishersShouldNotClaimSameEvent() throws Exception {
        /*
         * Create a fresh batch of pending events for this test.
         */
        List<OutboxEvent> createdEvents = transactionTemplate.execute(status ->
                repository.saveAllAndFlush(
                        java.util.stream.IntStream.range(0, 20)
                                .mapToObj(index -> OutboxEvent.create(
                                        UUID.randomUUID(),
                                        "TEST",
                                        "PAYMENT_COMPLETED",
                                        "{\"testIndex\":" + index + "}",
                                        Instant.now().plusMillis(index)
                                ))
                                .toList()
                )
        );

        assert createdEvents != null;

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<List<OutboxEvent>> firstPublisher =
                    executor.submit(claimService::claimPendingBatch);

            Future<List<OutboxEvent>> secondPublisher =
                    executor.submit(claimService::claimPendingBatch);

            Set<UUID> firstIds = ids(firstPublisher.get());
            Set<UUID> secondIds = ids(secondPublisher.get());

            Set<UUID> overlap = new HashSet<>(firstIds);
            overlap.retainAll(secondIds);

            assertTrue(
                    overlap.isEmpty(),
                    "Two concurrent publishers claimed the same event IDs: "
                            + overlap
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private Set<UUID> ids(List<OutboxEvent> events) {
        return events.stream()
                .map(OutboxEvent::getId)
                .collect(java.util.stream.Collectors.toSet());
    }
}
