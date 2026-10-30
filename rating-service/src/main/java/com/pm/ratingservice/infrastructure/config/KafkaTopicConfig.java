package com.pm.ratingservice.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic ratingEventsTopic(
            @Value("${rating.kafka.events-topic}")
            String topic
    ) {
        return new NewTopic(
                topic,
                3,
                (short) 1
        );
    }
}