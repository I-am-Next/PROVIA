package com.pm.searchservice.infrastructure.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaErrorHandlerConfig.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate
    ) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> {

                            String deadLetterTopic =
                                    record.topic() + ".DLT";

                            log.error(
                                    "Routing failed Kafka event to DLT. " +
                                            "sourceTopic={}, destinationTopic={}, " +
                                            "sourcePartition={}, offset={}",
                                    record.topic(),
                                    deadLetterTopic,
                                    record.partition(),
                                    record.offset(),
                                    exception
                            );

                            /*
                             * Partition -1 means no specific partition
                             * is selected by this resolver.
                             *
                             * Kafka can choose an available partition
                             * in the destination topic.
                             */
                            return new TopicPartition(
                                    deadLetterTopic,
                                    -1
                            );
                        }
                );

        FixedBackOff backOff =
                new FixedBackOff(
                        2_000L,
                        3L
                );

        return new DefaultErrorHandler(
                recoverer,
                backOff
        );
    }
}