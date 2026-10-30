
package com.pm.servicecatalog.infrastructure.outbox;

import com.pm.servicecatalog.infrastructure.kafka.CatalogEventProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxEventPublisher.class);

    private static final int SEND_TIMEOUT_SECONDS = 10;

    private final OutboxEventTransactionService transactionService;
    private final CatalogEventProducer producer;

    public OutboxEventPublisher(
            OutboxEventTransactionService transactionService,
            CatalogEventProducer producer
    ) {
        this.transactionService = transactionService;
        this.producer = producer;
    }

    /**
     * The scheduled method intentionally has no @Transactional.
     *
     * Database claims and status changes are handled by a separate
     * Spring bean. Kafka network calls therefore happen outside
     * those database transactions.
     */
    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        List<OutboxEvent> events =
                transactionService.claimReadyEvents();

        if (events.isEmpty()) {
            return;
        }

        log.info(
                "Publishing {} claimed catalog outbox events",
                events.size()
        );

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {

        UUID eventId = event.getId();

        try {
            byte[] payload = event.getPayload()
                    .getBytes(StandardCharsets.UTF_8);

            SendResult<String, byte[]> result =
                    sendEvent(event).get(
                            SEND_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS
                    );

            RecordMetadata metadata =
                    result.getRecordMetadata();

            /*
             * Kafka has acknowledged the send. Mark the event
             * processed in a new, short database transaction.
             */
            transactionService.markProcessed(eventId);

            log.info(
                    "Catalog outbox event published. " +
                            "id={}, type={}, aggregateType={}, " +
                            "aggregateId={}, partition={}, offset={}",
                    eventId,
                    event.getEventType(),
                    event.getAggregateType(),
                    event.getAggregateId(),
                    metadata.partition(),
                    metadata.offset()
            );

        } catch (Exception ex) {
            log.error(
                    "Failed to publish catalog outbox event. " +
                            "id={}, type={}, aggregateType={}, retryCount={}",
                    eventId,
                    event.getEventType(),
                    event.getAggregateType(),
                    event.getRetryCount(),
                    ex
            );

            try {
                transactionService.scheduleRetry(eventId);
            } catch (Exception statusException) {
                /*
                 * If PostgreSQL is unavailable, the status update
                 * can fail too. Leave recovery to the outbox
                 * recovery mechanism rather than hiding this error.
                 */
                log.error(
                        "Could not schedule retry for catalog outbox event. id={}",
                        eventId,
                        statusException
                );
            }
        }
    }

    private java.util.concurrent.CompletableFuture<
            SendResult<String, byte[]>> sendEvent(OutboxEvent event) {

        byte[] payload = event.getPayload()
                .getBytes(StandardCharsets.UTF_8);

        return switch (event.getAggregateType()) {
            case "CATEGORY" ->
                    producer.publishCategoryEvent(
                            event.getEventType(),
                            event.getAggregateId(),
                            payload
                    );

            case "SERVICE_ITEM" ->
                    producer.publishServiceItemEvent(
                            event.getEventType(),
                            event.getAggregateId(),
                            payload
                    );

            default -> throw new IllegalStateException(
                    "Unknown catalog aggregate type: "
                            + event.getAggregateType()
            );
        };
    }
}
