package com.pm.providerservice.infrastructure.outbox;

import com.pm.providerservice.infrastructure.messaging.ProviderEventProducer;
import com.pm.providerservice.infrastructure.messaging.ProviderEventTopicResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository repository;
    private final ProviderEventProducer producer;
    private final ProviderEventTopicResolver topicResolver;

    public OutboxPublisher(
            OutboxEventRepository repository,
            ProviderEventProducer producer,
            ProviderEventTopicResolver topicResolver
    ) {
        this.repository = repository;
        this.producer = producer;
        this.topicResolver = topicResolver;
    }

    @Scheduled(fixedDelay = 5000)
    public void publish() {

        List<OutboxEvent> events =
                repository.findPendingEvents();

        if (events.isEmpty()) {
            return;
        }

        log.info(
                "Processing {} provider outbox events",
                events.size()
        );

        for (OutboxEvent event : events) {

            try {

                String topic =
                        topicResolver.resolve(
                                event.getAggregateType()
                        );

                byte[] payload =
                        event.getPayload()
                                .getBytes(StandardCharsets.UTF_8);

                producer.publish(
                        topic,
                        event.getAggregateId().toString(),
                        payload
                );

                event.markSuccess();

                log.info(
                        "Provider outbox event published. " +
                                "id={}, type={}, aggregateType={}, " +
                                "aggregateId={}, topic={}",
                        event.getId(),
                        event.getEventType(),
                        event.getAggregateType(),
                        event.getAggregateId(),
                        topic
                );

            } catch (Exception ex) {

                event.markFailed(
                        ex.getMessage()
                );

                log.error(
                        "Failed to publish provider outbox event. " +
                                "id={}, type={}, aggregateType={}, " +
                                "aggregateId={}, retryCount={}",
                        event.getId(),
                        event.getEventType(),
                        event.getAggregateType(),
                        event.getAggregateId(),
                        event.getRetryCount(),
                        ex
                );
            }
        }

        repository.saveAll(events);
    }
}