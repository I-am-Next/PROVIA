package com.pm.notificationservice.delivery.template;

import org.springframework.stereotype.Component;

@Component
public class ChatTemplates {

    public String newMessageReceived() {

        return """
                You have received a new message.
                Open the conversation to reply.
                """;

    }

}