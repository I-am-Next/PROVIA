package com.pm.contracts.wallet;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawalCompletedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID withdrawalId,

        UUID walletId,

        UUID providerId,

        BigDecimal amount

) implements DomainEvent {

    @Override
    public String eventType() {

        return "WITHDRAWAL_COMPLETED";

    }

    @Override
    public UUID aggregateId() {

        return withdrawalId;

    }

    @Override
    public String aggregateType() {

        return "WITHDRAWAL";

    }

}