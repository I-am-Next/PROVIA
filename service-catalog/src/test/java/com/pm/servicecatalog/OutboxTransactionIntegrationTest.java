
package com.pm.servicecatalog;

import com.pm.servicecatalog.application.dto.CreateCategoryRequest;
import com.pm.servicecatalog.application.service.CreateCategoryService;
import com.pm.servicecatalog.domain.ports.out.ServiceCategoryRepository;
import com.pm.servicecatalog.infrastructure.outbox.OutboxEvent;
import com.pm.servicecatalog.infrastructure.outbox.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import jakarta.persistence.EntityManager;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@SpringBootTest
class OutboxTransactionIntegrationTest {

    @Autowired
    private CreateCategoryService createCategoryService;

    @Autowired
    private ServiceCategoryRepository categoryRepository;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private OutboxEventRepository outboxRepositorySpy;

    @Test
    void categoryCreationMustRollbackWhenOutboxSaveFails() {

        // Confirm this test is using real PostgreSQL.
        String databaseVersion =
                jdbcTemplate.queryForObject(
                        "SELECT version()",
                        String.class
                );

        assertNotNull(databaseVersion);
        assertTrue(
                databaseVersion.toLowerCase().contains("postgresql"),
                "This integration test must run against PostgreSQL"
        );

        String categoryName =
                "Outbox-Rollback-" + UUID.randomUUID();

        long initialOutboxCount = outboxRepository.count();

        /*
         * Simulate an outbox persistence failure after flushing
         * pending JPA changes. This ensures the category INSERT
         * reaches PostgreSQL before the exception is thrown.
         */
        doAnswer(invocation -> {
            entityManager.flush();

            throw new DataIntegrityViolationException(
                    "Simulated outbox persistence failure"
            );
        }).when(outboxRepositorySpy).save(any(OutboxEvent.class));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> createCategoryService.createCategory(
                        new CreateCategoryRequest(
                                categoryName,
                                "Integration test rollback"
                        )
                )
        );

        verify(outboxRepositorySpy).save(any(OutboxEvent.class));

        /*
         * The service transaction should have rolled back.
         * These reads run after the failed transaction has ended.
         */
        assertTrue(
                categoryRepository.findByName(categoryName).isEmpty(),
                "The category must be rolled back when outbox persistence fails"
        );

        assertEquals(
                initialOutboxCount,
                outboxRepository.count(),
                "No outbox event should remain from the failed transaction"
        );
    }
}
