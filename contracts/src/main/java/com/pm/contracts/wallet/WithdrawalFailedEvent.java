package com.pm.contracts.wallet;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawalFailedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID withdrawalId,

        UUID walletId,

        UUID providerId,

        BigDecimal amount,

        String reason

) implements DomainEvent {

    @Override
    public String eventType() {

        return "WITHDRAWAL_FAILED";

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