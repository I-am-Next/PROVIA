package com.pm.contracts.common;

public record EventEnvelope<T extends DomainEvent>(

        String type,

        int version,

        T payload

) {
}