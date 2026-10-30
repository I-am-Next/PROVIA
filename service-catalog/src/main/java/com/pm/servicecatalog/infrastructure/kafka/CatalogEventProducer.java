package com.pm.servicecatalog.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Component
public class CatalogEventProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CatalogEventProducer(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<SendResult<String, byte[]>>
    publishCategoryEvent(
            String eventType,
            String aggregateId,
            byte[] payload
    ) {
        validateEvent(eventType, aggregateId, payload);

        return kafkaTemplate.send(
                CatalogTopics.CATEGORY_EVENTS,
                aggregateId,
                payload
        );
    }

    public CompletableFuture<SendResult<String, byte[]>>
    publishServiceItemEvent(
            String eventType,
            String aggregateId,
            byte[] payload
    ) {
        validateEvent(eventType, aggregateId, payload);

        return kafkaTemplate.send(
                CatalogTopics.SERVICE_ITEM_EVENTS,
                aggregateId,
                payload
        );
    }

    private void validateEvent(
            String eventType,
            String aggregateId,
            byte[] payload
    ) {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException(
                    "Catalog event type must not be blank"
            );
        }

        if (aggregateId == null || aggregateId.isBlank()) {
            throw new IllegalArgumentException(
                    "Catalog event aggregate ID must not be blank"
            );
        }

        if (payload == null || payload.length == 0) {
            throw new IllegalArgumentException(
                    "Catalog event payload must not be empty"
            );
        }

        try {
            JsonNode root = objectMapper.readTree(
                    new String(payload, StandardCharsets.UTF_8)
            );

            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException(
                        "Catalog event must be a JSON object"
                );
            }

            JsonNode typeNode = root.get("type");
            JsonNode versionNode = root.get("version");
            JsonNode payloadNode = root.get("payload");

            if (typeNode == null
                    || !typeNode.isTextual()
                    || typeNode.asText().isBlank()) {
                throw new IllegalArgumentException(
                        "Catalog event envelope must contain a valid type"
                );
            }

            if (!eventType.equals(typeNode.asText())) {
                throw new IllegalArgumentException(
                        "Outbox event type does not match envelope type. "
                                + "outboxType=" + eventType
                                + ", envelopeType=" + typeNode.asText()
                );
            }

            if (versionNode == null
                    || !versionNode.canConvertToInt()) {
                throw new IllegalArgumentException(
                        "Catalog event envelope must contain a valid version"
                );
            }

            if (payloadNode == null || !payloadNode.isObject()) {
                throw new IllegalArgumentException(
                        "Catalog event envelope must contain an object payload"
                );
            }

        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Unable to parse catalog event envelope",
                    ex
            );
        }
    }
}