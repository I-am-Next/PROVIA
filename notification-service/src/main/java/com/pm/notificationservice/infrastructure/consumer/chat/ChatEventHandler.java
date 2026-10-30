package com.pm.notificationservice.infrastructure.consumer.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.chat.ChatMessageReceivedEvent;
import com.pm.contracts.common.EventEnvelope;
import com.pm.notificationservice.application.service.EventProcessingService;
import com.pm.notificationservice.application.service.NotificationCreationService;
import com.pm.notificationservice.infrastructure.consumer.AbstractEventHandler;
import org.springframework.stereotype.Component;

@Component
public class ChatEventHandler extends AbstractEventHandler {


    public ChatEventHandler(
            ObjectMapper objectMapper,
            NotificationCreationService notificationCreationService,
            EventProcessingService eventProcessingService
    ) {

        super(
                objectMapper,
                notificationCreationService,
                eventProcessingService
        );

    }


    public void handle(
            EventEnvelope<?> envelope
    ) {


        switch (envelope.type()) {


            case "CHAT_MESSAGE_RECEIVED" -> {

                ChatMessageReceivedEvent event =
                        convert(
                                envelope.payload(),
                                ChatMessageReceivedEvent.class
                        );


                process(
                        event,
                        () -> notificationCreationService.createChatMessageNotification(event)
                );

            }


            default -> throw new IllegalArgumentException(
                    "Unsupported chat event type: " + envelope.type()
            );

        }

    }

}