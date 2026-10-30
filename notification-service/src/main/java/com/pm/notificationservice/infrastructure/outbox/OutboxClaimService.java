package com.pm.notificationservice.infrastructure.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class OutboxClaimService {

    private final OutboxEventRepository repository;
    private final long claimTimeoutSeconds;

    public OutboxClaimService(
            OutboxEventRepository repository,
            @Value("${notification.outbox.claim-timeout-seconds:300}")
            long claimTimeoutSeconds
    ) {
        this.repository = repository;
        this.claimTimeoutSeconds = claimTimeoutSeconds;
    }

    @Transactional
    public List<OutboxEvent> claimPendingBatch() {
        Instant cutoff = Instant.now()
                .minus(claimTimeoutSeconds, ChronoUnit.SECONDS);

        repository.recoverStaleClaims(cutoff);

        List<UUID> ids = repository.claimPendingIds();

        if (ids.isEmpty()) {
            return List.of();
        }

        List<OutboxEvent> events = repository.findAllById(ids);
        Instant now = Instant.now();

        for (OutboxEvent event : events) {
            event.markProcessing(now);
        }

        return repository.saveAll(events);
    }
}
