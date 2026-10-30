package com.pm.paymentservicenew.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate(

            KafkaProperties kafkaProperties

    ) {

        Map<String, Object> properties = new HashMap<>(

                kafkaProperties.buildProducerProperties(null)

        );

        properties.put(

                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,

                StringSerializer.class

        );

        properties.put(

                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,

                ByteArraySerializer.class

        );

        return new KafkaTemplate<>(

                new DefaultKafkaProducerFactory<>(

                        properties

                )

        );

    }

}