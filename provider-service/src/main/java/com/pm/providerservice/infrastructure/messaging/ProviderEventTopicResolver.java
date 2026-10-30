package com.pm.providerservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

@Component
public class ProviderEventTopicResolver {

    public String resolve(String aggregateType) {

        return switch (aggregateType) {

            case "PROVIDER" ->
                    ProviderTopics.PROVIDER_EVENTS;

            case "PROVIDER_SERVICE" ->
                    ProviderTopics.PROVIDER_SERVICE_EVENTS;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported provider aggregate type: "
                                    + aggregateType
                    );
        };
    }
}