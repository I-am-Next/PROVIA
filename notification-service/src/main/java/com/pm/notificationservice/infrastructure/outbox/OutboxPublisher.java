package com.pm.notificationservice.infrastructure.outbox;

import com.pm.notificationservice.infrastructure.producer.KafkaTopicResolver;
import com.pm.notificationservice.infrastructure.producer.NotificationEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxClaimService claimService;
    private final NotificationEventProducer producer;
    private final KafkaTopicResolver topicResolver;
    private final OutboxStatusService statusService;

    public OutboxPublisher(
            OutboxClaimService claimService,
            NotificationEventProducer producer,
            KafkaTopicResolver topicResolver,
            OutboxStatusService statusService
    ) {
        this.claimService = Objects.requireNonNull(claimService);
        this.producer = Objects.requireNonNull(producer);
        this.topicResolver = Objects.requireNonNull(topicResolver);
        this.statusService = Objects.requireNonNull(statusService);
    }

    @Scheduled(
            fixedDelayString = "${notification.outbox.publish-delay:5000}"
    )
    public void publishPendingEvents() {
        List<OutboxEvent> events = claimService.claimPendingBatch();

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {
        try {
            NotificationOutboxEventType eventType =
                    NotificationOutboxEventType.valueOf(
                            event.getEventType()
                    );

            String topic = topicResolver.resolve(eventType);

            String key = Objects.requireNonNull(
                    event.getAggregateId(),
                    "Outbox event aggregateId must not be null"
            ).toString();

            producer.publish(
                    topic,
                    key,
                    event.getPayload()
            );

            statusService.markPublished(event.getId());

        } catch (Exception ex) {
            log.error(
                    "Failed to publish outbox event. eventId={}, " +
                            "eventType={}, retryCount={}",
                    event.getId(),
                    event.getEventType(),
                    event.getRetryCount(),
                    ex
            );

            try {
                statusService.markFailed(
                        event.getId(),
                        ex.getMessage()
                );
            } catch (Exception statusException) {
                log.error(
                        "Failed to update outbox status. eventId={}",
                        event.getId(),
                        statusException
                );
            }
        }
    }
}
