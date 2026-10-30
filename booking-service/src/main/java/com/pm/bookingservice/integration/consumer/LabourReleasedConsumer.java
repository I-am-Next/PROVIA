package com.pm.bookingservice.integration.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.bookingservice.application.service.LabourReleasedEventHandler;
import com.pm.contracts.escrow.LabourReleasedEvent;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class LabourReleasedConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(LabourReleasedConsumer.class);

    private static final String LABOUR_RELEASED =
            "LABOUR_RELEASED";

    private static final int SUPPORTED_ENVELOPE_VERSION = 1;

    private final ObjectMapper objectMapper;
    private final LabourReleasedEventHandler eventHandler;

    public LabourReleasedConsumer(
            ObjectMapper objectMapper,
            LabourReleasedEventHandler eventHandler
    ) {
        this.objectMapper = objectMapper;
        this.eventHandler = eventHandler;
    }

    @KafkaListener(
            topics = "${booking.kafka.escrow-events-topic:escrow-events}",
            groupId = "${booking.kafka.labour-released-group-id:${spring.kafka.consumer.group-id}-labour-released}"
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

            if (!LABOUR_RELEASED.equals(type)) {
                log.debug(
                        "Ignoring escrow event type={} in labour consumer.",
                        type
                );
                return;
            }

            int version = versionNode.asInt();

            if (version != SUPPORTED_ENVELOPE_VERSION) {
                throw new SerializationException(
                        "Unsupported LABOUR_RELEASED envelope version: "
                                + version
                                + ". Expected: "
                                + SUPPORTED_ENVELOPE_VERSION
                );
            }

            LabourReleasedEvent event =
                    objectMapper.treeToValue(
                            payloadNode,
                            LabourReleasedEvent.class
                    );

            if (event == null
                    || !LABOUR_RELEASED.equals(event.eventType())
                    || event.eventVersion() != SUPPORTED_ENVELOPE_VERSION) {
                throw new SerializationException(
                        "LABOUR_RELEASED envelope and payload versions "
                                + "or event types do not match."
                );
            }

            log.info(
                    "Received LABOUR_RELEASED event. eventId={}, "
                            + "bookingId={}, escrowId={}, paymentId={}",
                    event.eventId(),
                    event.bookingId(),
                    event.escrowId(),
                    event.paymentId()
            );

            eventHandler.handle(event);

        } catch (Exception ex) {
            log.error(
                    "Failed to process LABOUR_RELEASED event. payload={}",
                    message,
                    ex
            );

            // Propagate the exception so Kafka's configured error handler
            // can retry the record or route it to the dead-letter topic.
            throw ex;
        }
    }
}