package com.pm.paymentservicenew.domain.model.wallet;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID walletId;

    @Column(nullable = false)
    private UUID providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletTransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private UUID referenceId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected WalletTransaction() {
    }

    private WalletTransaction(
            UUID walletId,
            UUID providerId,
            WalletTransactionType type,
            BigDecimal amount,
            UUID referenceId,
            String description
    ) {

        this.walletId = walletId;
        this.providerId = providerId;
        this.type = type;
        this.amount = amount;
        this.referenceId = referenceId;
        this.description = description;
        this.occurredAt = Instant.now();

    }

    public static WalletTransaction create(
            UUID walletId,
            UUID providerId,
            WalletTransactionType type,
            BigDecimal amount,
            UUID referenceId,
            String description
    ) {

        return new WalletTransaction(
                walletId,
                providerId,
                type,
                amount,
                referenceId,
                description
        );

    }

    public UUID getId() {
        return id;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public WalletTransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

}