package com.pm.notificationservice.infrastructure.consumer.booking;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
class BookingEventsConsumerKafkaIntegrationTest {

    private static final String BOOKING_TOPIC =
            "booking-events";

    private static final String DLT_TOPIC =
            "booking-events.DLT";

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(
                    DockerImageName.parse(
                            "apache/kafka:3.8.0"
                    )
            );

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:16-alpine"
            )
                    .withDatabaseName(
                            "notification_test"
                    )
                    .withUsername(
                            "notification_test"
                    )
                    .withPassword(
                            "notification_test"
                    );

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoSpyBean
    private BookingEventsConsumer bookingEventsConsumer;

    private static KafkaConsumer<String, String> dltConsumer;

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {

        registry.add(
                "spring.kafka.bootstrap-servers",
                KAFKA::getBootstrapServers
        );

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
                "notification.kafka.bookings",
                () -> BOOKING_TOPIC
        );

        registry.add(
                "spring.kafka.consumer.group-id",
                () -> "notification-kafka-test-"
                        + UUID.randomUUID()
        );
    }

    @BeforeAll
    static void createTopics() throws Exception {

        Properties adminProperties =
                new Properties();

        adminProperties.put(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                KAFKA.getBootstrapServers()
        );

        try (AdminClient adminClient =
                     AdminClient.create(adminProperties)) {

            adminClient.createTopics(
                    List.of(
                            new NewTopic(
                                    BOOKING_TOPIC,
                                    1,
                                    (short) 1
                            ),
                            new NewTopic(
                                    DLT_TOPIC,
                                    1,
                                    (short) 1
                            )
                    )
            ).all().get(
                    10,
                    TimeUnit.SECONDS
            );
        }

        dltConsumer =
                createDltConsumer();

        dltConsumer.subscribe(
                Collections.singletonList(
                        DLT_TOPIC
                )
        );

        /*
         * Force the consumer to obtain its assignment
         * before the actual event is published.
         */
        dltConsumer.poll(
                Duration.ofMillis(500)
        );
    }

    @AfterAll
    static void closeDltConsumer() {

        if (dltConsumer != null) {
            dltConsumer.close();
        }
    }

    @Test
    void failedBookingEvent_isRetriedThreeTimes_thenPublishedToDlt()
            throws Exception {

        /*
         * This event is intentionally malformed.
         *
         * BOOKING_CREATED is a supported event type, so
         * BookingEventsConsumer will attempt to deserialize
         * the empty payload into BookingCreatedEvent.
         *
         * The empty payload should cause the real listener
         * to throw an exception.
         */
        String message =
                """
                {
                  "type": "BOOKING_CREATED",
                  "version": 1,
                  "payload": {}
                }
                """;

        kafkaTemplate
                .send(
                        BOOKING_TOPIC,
                        "test-booking-key",
                        message
                )
                .get(
                        10,
                        TimeUnit.SECONDS
                );

        /*
         * DefaultErrorHandler is configured with:
         *
         * FixedBackOff(3000L, 3L)
         *
         * Therefore:
         *
         * initial delivery
         * + retry 1
         * + retry 2
         * + retry 3
         *
         * = 4 total listener invocations.
         */
        verify(
                bookingEventsConsumer,
                timeout(20_000)
                        .times(4)
        ).consume(message);

        /*
         * After the retries are exhausted,
         * DeadLetterPublishingRecoverer should publish
         * the record to booking-events.DLT.
         */
        ConsumerRecord<String, String> dltRecord =
                pollDltRecord(
                        message,
                        Duration.ofSeconds(20)
                );

        assertThat(dltRecord)
                .isNotNull();

        assertThat(dltRecord.topic())
                .isEqualTo(DLT_TOPIC);

        assertThat(dltRecord.key())
                .isEqualTo("test-booking-key");

        assertThat(dltRecord.value())
                .isEqualTo(message);
    }

    private static KafkaConsumer<String, String> createDltConsumer() {

        Properties properties =
                new Properties();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KAFKA.getBootstrapServers()
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "dlt-test-"
                        + UUID.randomUUID()
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                "false"
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        return new KafkaConsumer<>(
                properties
        );
    }

    private ConsumerRecord<String, String> pollDltRecord(
            String expectedMessage,
            Duration timeout
    ) {

        long deadline =
                System.nanoTime()
                        + timeout.toNanos();

        while (
                System.nanoTime()
                        < deadline
        ) {

            var records =
                    dltConsumer.poll(
                            Duration.ofSeconds(1)
                    );

            for (
                    ConsumerRecord<String, String> record
                    : records
            ) {

                if (
                        expectedMessage.equals(
                                record.value()
                        )
                ) {
                    return record;
                }
            }
        }

        return null;
    }
}