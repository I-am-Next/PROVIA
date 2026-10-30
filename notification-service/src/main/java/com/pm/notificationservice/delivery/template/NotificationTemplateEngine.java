package com.pm.notificationservice.delivery.template;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationTemplateEngine {

    public String render(
            String template,
            Map<String, Object> variables
    ) {

        String result = template;

        for (Map.Entry<String, Object> entry : variables.entrySet()) {

            String placeholder =
                    "{{" + entry.getKey() + "}}";

            result = result.replace(
                    placeholder,
                    String.valueOf(entry.getValue())
            );

        }

        return result;

    }

}