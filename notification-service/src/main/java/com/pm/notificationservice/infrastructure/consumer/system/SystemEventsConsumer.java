package com.pm.notificationservice.infrastructure.consumer.system;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.EventEnvelope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SystemEventsConsumer {

    private final ObjectMapper objectMapper;

    private final SystemEventHandler systemEventHandler;


    public SystemEventsConsumer(
            ObjectMapper objectMapper,
            SystemEventHandler systemEventHandler
    ) {
        this.objectMapper = objectMapper;
        this.systemEventHandler = systemEventHandler;
    }


    @KafkaListener(
            topics = "${notification.kafka.system}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            String message
    ) throws Exception {


        EventEnvelope<?> envelope =
                objectMapper.readValue(
                        message,
                        new TypeReference<>() {
                        }
                );


        systemEventHandler.handle(
                envelope
        );

    }

}