package com.pm.searchservice.infrastructure.consumer.rating;

import com.pm.searchservice.infrastructure.consumer.SearchEventProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class RatingEventsConsumer {

    private final SearchEventProcessor eventProcessor;

    public RatingEventsConsumer(
            SearchEventProcessor eventProcessor
    ) {
        this.eventProcessor = eventProcessor;
    }

    @KafkaListener(
            topics = "${search.kafka.rating-events-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC)
            String topic
    ) {

        eventProcessor.processFromKafka(
                topic,
                message
        );
    }
}