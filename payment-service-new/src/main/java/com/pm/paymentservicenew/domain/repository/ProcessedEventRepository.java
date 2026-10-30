package com.pm.paymentservicenew.domain.repository;

import com.pm.paymentservicenew.domain.model.idempotency.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEvent, UUID> {

    boolean existsByProviderAndIdempotencyKeyAndEventType(

            String provider,

            String idempotencyKey,

            String eventType

    );

}