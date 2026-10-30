package com.pm.ratingservice.integration.producer;

import com.pm.ratingservice.infrastructure.outbox.OutboxEvent;
import com.pm.ratingservice.infrastructure.outbox.OutboxEventType;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class RatingEventProducer {

    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final KafkaTopicResolver topicResolver;

    public RatingEventProducer(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            KafkaTopicResolver topicResolver
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicResolver = topicResolver;
    }

    public void publish(OutboxEvent event) throws Exception {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Outbox event must not be null"
            );
        }

        OutboxEventType eventType;

        try {
            eventType = OutboxEventType.valueOf(
                    event.getEventType()
            );
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(
                    "Unsupported rating outbox event type: "
                            + event.getEventType(),
                    ex
            );
        }

        String topic = topicResolver.resolve(eventType);

        String key = event.getAggregateId().toString();

        byte[] payload = event.getPayload().getBytes(
                StandardCharsets.UTF_8
        );

        SendResult<String, byte[]> result;

        try {
            result = kafkaTemplate.send(
                    topic,
                    key,
                    payload
            ).get(
                    SEND_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );
        } catch (TimeoutException ex) {
            throw new IllegalStateException(
                    "Timed out waiting for Kafka acknowledgement for "
                            + event.getId(),
                    ex
            );
        }

        RecordMetadata metadata = result.getRecordMetadata();

        if (metadata == null) {
            throw new IllegalStateException(
                    "Kafka returned no record metadata for outbox event "
                            + event.getId()
            );
        }
    }
}