package com.pm.providerservice.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class ProviderEventProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public ProviderEventProducer(
            KafkaTemplate<String, byte[]> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(
            String topic,
            String aggregateId,
            byte[] payload
    ) {

        try {

            kafkaTemplate
                    .send(
                            topic,
                            aggregateId,
                            payload
                    )
                    .get();

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while publishing provider event",
                    ex
            );

        } catch (ExecutionException ex) {

            throw new IllegalStateException(
                    "Failed to publish provider event to Kafka",
                    ex.getCause()
            );
        }
    }
}