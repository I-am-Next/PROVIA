package com.pm.bookingservice.integration.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.bookingservice.application.service.PaymentEventHandler;
import com.pm.contracts.common.EventEnvelope;
import com.pm.contracts.payment.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PaymentCompletedConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PaymentCompletedConsumer.class
            );

    private final ObjectMapper objectMapper;

    private final PaymentEventHandler paymentEventHandler;

    public PaymentCompletedConsumer(
            ObjectMapper objectMapper,
            PaymentEventHandler paymentEventHandler
    ) {

        this.objectMapper = objectMapper;
        this.paymentEventHandler = paymentEventHandler;
    }

    @KafkaListener(
            topics = "${booking.kafka.payment-events-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            byte[] payload
    ) throws Exception {

        log.info(
                "========== PAYMENT COMPLETED EVENT RECEIVED =========="
        );

        String message =
                new String(
                        payload,
                        StandardCharsets.UTF_8
                );

        log.info(
                "Raw Kafka payload: {}",
                message
        );

        try {

            JsonNode root =
                    objectMapper.readTree(message);

            String type =
                    root.get("type").asText();

            int version =
                    root.get("version").asInt();

            JsonNode payloadNode =
                    root.get("payload");

            log.info(
                    "Detected payment event type={}, version={}",
                    type,
                    version
            );

            /*
             * This consumer is specifically responsible
             * for PAYMENT_COMPLETED.
             */
            if (!"PAYMENT_COMPLETED".equals(type)) {

                log.info(
                        "Ignoring payment event type={} because " +
                                "Booking Service only handles PAYMENT_COMPLETED.",
                        type
                );

                return;
            }

            PaymentCompletedEvent event =
                    objectMapper.treeToValue(
                            payloadNode,
                            PaymentCompletedEvent.class
                    );

            EventEnvelope<PaymentCompletedEvent> envelope =
                    new EventEnvelope<>(
                            type,
                            version,
                            event
                    );

            log.info(
                    "Deserialized PaymentCompletedEvent: " +
                            "eventId={}, paymentId={}, bookingId={}, providerId={}",
                    event.eventId(),
                    event.paymentId(),
                    event.bookingId(),
                    event.providerId()
            );

            log.info(
                    "Passing PAYMENT_COMPLETED event to PaymentEventHandler..."
            );

            paymentEventHandler.handle(
                    envelope.payload()
            );

            log.info(
                    "========== PAYMENT COMPLETED EVENT HANDLED =========="
            );

        } catch (Exception ex) {

            log.error(
                    "❌ FAILED PROCESSING PAYMENT_COMPLETED EVENT. " +
                            "Raw payload={}",
                    message,
                    ex
            );

            throw ex;
        }
    }
}