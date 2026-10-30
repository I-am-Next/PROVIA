package com.pm.searchservice.infrastructure.consumer.catalog;

import com.pm.searchservice.infrastructure.consumer.SearchEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class CatalogEventsConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(CatalogEventsConsumer.class);

    private final SearchEventProcessor eventProcessor;

    public CatalogEventsConsumer(
            SearchEventProcessor eventProcessor
    ) {
        this.eventProcessor = eventProcessor;
    }

    @KafkaListener(
            topics = {
                    "${search.kafka.catalog-category-events-topic}",
                    "${search.kafka.catalog-service-item-events-topic}"
            },
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC)
            String topic
    ) {
        log.info(
                "Catalog Kafka message received. topic={}, payloadBytes={}",
                topic,
                message == null
                        ? 0
                        : message.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                ).length
        );

        eventProcessor.processFromKafka(topic, message);

        log.info(
                "Catalog Kafka message processing completed. topic={}",
                topic
        );
    }
}