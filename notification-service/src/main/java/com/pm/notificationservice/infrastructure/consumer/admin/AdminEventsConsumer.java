package com.pm.notificationservice.infrastructure.consumer.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.EventEnvelope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AdminEventsConsumer {

    private final ObjectMapper objectMapper;

    private final AdminEventHandler adminEventHandler;


    public AdminEventsConsumer(
            ObjectMapper objectMapper,
            AdminEventHandler adminEventHandler
    ) {
        this.objectMapper = objectMapper;
        this.adminEventHandler = adminEventHandler;
    }


    @KafkaListener(
            topics = "${notification.kafka.admin}",
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


        adminEventHandler.handle(
                envelope
        );

    }

}