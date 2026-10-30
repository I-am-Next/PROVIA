package com.pm.notificationservice.infrastructure.consumer.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.EventEnvelope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ChatEventsConsumer {

    private final ObjectMapper objectMapper;

    private final ChatEventHandler chatEventHandler;


    public ChatEventsConsumer(
            ObjectMapper objectMapper,
            ChatEventHandler chatEventHandler
    ) {
        this.objectMapper = objectMapper;
        this.chatEventHandler = chatEventHandler;
    }


    @KafkaListener(
            topics = "${notification.kafka.chats}",
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


        chatEventHandler.handle(
                envelope
        );

    }

}