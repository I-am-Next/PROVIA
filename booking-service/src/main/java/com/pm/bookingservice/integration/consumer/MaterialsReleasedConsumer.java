package com.pm.bookingservice.integration.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.bookingservice.application.service.MaterialsReleasedEventHandler;
import com.pm.contracts.escrow.MaterialsReleasedEvent;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MaterialsReleasedConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(MaterialsReleasedConsumer.class);

    private static final String MATERIALS_RELEASED =
            "MATERIALS_RELEASED";

    private static final int SUPPORTED_ENVELOPE_VERSION = 2;

    private final ObjectMapper objectMapper;
    private final MaterialsReleasedEventHandler eventHandler;

    public MaterialsReleasedConsumer(
            ObjectMapper objectMapper,
            MaterialsReleasedEventHandler eventHandler
    ) {
        this.objectMapper = objectMapper;
        this.eventHandler = eventHandler;
    }

    @KafkaListener(
            topics = "${booking.kafka.escrow-events-topic:escrow-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(byte[] payload) throws Exception {

        if (payload == null || payload.length == 0) {
            throw new SerializationException(
                    "Received an empty escrow event payload."
            );
        }

        String message = new String(
                payload,
                StandardCharsets.UTF_8
        );

        try {
            JsonNode root = objectMapper.readTree(message);

            if (root == null || !root.isObject()) {
                throw new SerializationException(
                        "Escrow event envelope must be a JSON object."
                );
            }

            JsonNode typeNode = root.get("type");
            JsonNode versionNode = root.get("version");
            JsonNode payloadNode = root.get("payload");

            if (typeNode == null
                    || !typeNode.isTextual()
                    || versionNode == null
                    || !versionNode.canConvertToInt()
                    || payloadNode == null
                    || !payloadNode.isObject()) {
                throw new SerializationException(
                        "Escrow event envelope is missing valid required fields."
                );
            }

            String type = typeNode.asText();

            if (!MATERIALS_RELEASED.equals(type)) {
                log.debug(
                        "Ignoring escrow event type={} in materials consumer.",
                        type
                );
                return;
            }

            int version = versionNode.asInt();

            if (version != SUPPORTED_ENVELOPE_VERSION) {
                throw new SerializationException(
                        "Unsupported MATERIALS_RELEASED envelope version: "
                                + version
                                + ". Expected: "
                                + SUPPORTED_ENVELOPE_VERSION
                );
            }

            MaterialsReleasedEvent event =
                    objectMapper.treeToValue(
                            payloadNode,
                            MaterialsReleasedEvent.class
                    );

            if (event == null
                    || !MATERIALS_RELEASED.equals(event.eventType())
                    || event.eventVersion() != SUPPORTED_ENVELOPE_VERSION) {
                throw new SerializationException(
                        "MATERIALS_RELEASED envelope and payload versions "
                                + "or event types do not match."
                );
            }

            eventHandler.handle(event);

        } catch (Exception ex) {
            log.error(
                    "Failed to process MATERIALS_RELEASED event. payload={}",
                    message,
                    ex
            );

            /*
             * Propagate failures so the configured Kafka error handler
             * can retry or route the record to its DLT.
             */
            throw ex;
        }
    }
}
