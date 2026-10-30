package com.pm.contracts.wallet;

import com.pm.contracts.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletCreditedEvent(
        UUID eventId,
        int eventVersion,
        Instant occurredAt,
        UUID walletId,
        UUID providerId,
        UUID operationId,
        UUID escrowId,
        BigDecimal amount,
        BigDecimal availableBalance
) implements DomainEvent {

    @Override
    public String eventType() {
        return "WALLET_CREDITED";
    }

    @Override
    public UUID aggregateId() {
        return operationId;
    }

    @Override
    public String aggregateType() {
        return "WALLET_OPERATION";
    }
}
