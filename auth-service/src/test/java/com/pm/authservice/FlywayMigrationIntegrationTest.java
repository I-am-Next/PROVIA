package com.pm.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class FlywayMigrationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("auth_migration_test")
                    .withUsername("auth_test_user")
                    .withPassword("auth_test_password");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
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
                "spring.flyway.enabled",
                () -> true
        );
        registry.add(
                "spring.flyway.baseline-on-migrate",
                () -> false
        );
        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "validate"
        );

        // Test-only secret. Never use this value outside tests.
        registry.add(
                "jwt.secret",
                () -> "test-only-secret-key-for-auth-service"
        );

        registry.add(
                "spring.kafka.bootstrap-servers",
                () -> "localhost:9092"
        );
        registry.add(
                "spring.data.redis.host",
                () -> "localhost"
        );
        registry.add(
                "spring.data.redis.port",
                () -> 6379
        );
    }

    @Test
    void shouldRunV1MigrationAndCreateAuthTables() {
        Integer usersTableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'users'
                """,
                Integer.class
        );

        Integer refreshTokensTableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'refresh_tokens'
                """,
                Integer.class
        );

        assertThat(usersTableCount).isEqualTo(1);
        assertThat(refreshTokensTableCount).isEqualTo(1);
    }

    @Test
    void shouldRecordSuccessfulV1Migration() {
        Integer successfulV1Migrations = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '1'
                  AND type = 'SQL'
                  AND success = TRUE
                """,
                Integer.class
        );

        assertThat(successfulV1Migrations).isEqualTo(1);
    }

    @Test
    void shouldCreateRequiredConstraints() {
        Integer usersPrimaryKeyCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.table_constraints
                WHERE table_schema = 'public'
                  AND table_name = 'users'
                  AND constraint_type = 'PRIMARY KEY'
                """,
                Integer.class
        );

        Integer refreshTokenPrimaryKeyCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.table_constraints
                WHERE table_schema = 'public'
                  AND table_name = 'refresh_tokens'
                  AND constraint_type = 'PRIMARY KEY'
                """,
                Integer.class
        );

        assertThat(usersPrimaryKeyCount).isEqualTo(1);
        assertThat(refreshTokenPrimaryKeyCount).isEqualTo(1);
    }
}