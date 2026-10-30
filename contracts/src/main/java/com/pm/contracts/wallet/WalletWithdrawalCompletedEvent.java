package com.pm.contracts.wallet;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletWithdrawalCompletedEvent(
        UUID eventId,
        int eventVersion,
        Instant occurredAt,
        UUID walletId,
        UUID providerId,
        UUID withdrawalId,
        BigDecimal amount
) implements DomainEvent {

    @Override
    public String eventType() {
        return "WALLET_WITHDRAWAL_COMPLETED";
    }

    @Override
    public UUID aggregateId() {
        return withdrawalId;
    }

    @Override
    public String aggregateType() {
        return "WALLET_OPERATION";
    }
}
