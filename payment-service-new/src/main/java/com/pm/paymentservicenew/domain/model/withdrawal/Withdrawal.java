package com.pm.paymentservicenew.domain.model.withdrawal;

import com.pm.paymentservicenew.domain.exception.InvalidAmountException;
import com.pm.paymentservicenew.domain.exception.InvalidWithdrawalStateException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "withdrawals",
        indexes = {
                @Index(name = "idx_withdrawal_wallet", columnList = "walletId"),
                @Index(name = "idx_withdrawal_provider", columnList = "providerId"),
                @Index(name = "idx_withdrawal_status", columnList = "status")
        }
)
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false)
    private UUID walletId;

    @Column(nullable = false)
    private UUID providerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawalStatus status;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private Instant requestedAt;

    private Instant processedAt;

    private Instant completedAt;

    private Instant failedAt;

    private Instant cancelledAt;

    protected Withdrawal() {
    }

    private Withdrawal(
            UUID walletId,
            UUID providerId,
            BigDecimal amount
    ) {

        validateAmount(amount);

        this.walletId = walletId;
        this.providerId = providerId;
        this.amount = amount;
        this.status = WithdrawalStatus.PENDING;
        this.requestedAt = Instant.now();

    }

    public static Withdrawal create(
            UUID walletId,
            UUID providerId,
            BigDecimal amount
    ) {

        return new Withdrawal(
                walletId,
                providerId,
                amount
        );

    }

    /*
     * Getters
     */

    public UUID getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public WithdrawalStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    /*
     * State helpers
     */

    public boolean isPending() {
        return status == WithdrawalStatus.PENDING;
    }

    public boolean isProcessing() {
        return status == WithdrawalStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return status == WithdrawalStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == WithdrawalStatus.FAILED;
    }

    public boolean isCancelled() {
        return status == WithdrawalStatus.CANCELLED;
    }

    /*
     * State transitions
     */

    public void startProcessing() {

        if (!isPending()) {

            throw new InvalidWithdrawalStateException(
                    String.format(
                            "Cannot process withdrawal when status is %s.",
                            status
                    )
            );

        }

        status = WithdrawalStatus.PROCESSING;
        processedAt = Instant.now();

    }

    public void complete() {

        if (!isProcessing()) {

            throw new InvalidWithdrawalStateException(
                    String.format(
                            "Cannot complete withdrawal when status is %s.",
                            status
                    )
            );

        }

        status = WithdrawalStatus.COMPLETED;
        completedAt = Instant.now();

    }

    public void fail(
            String reason
    ) {

        if (!isProcessing()) {

            throw new InvalidWithdrawalStateException(
                    String.format(
                            "Cannot fail withdrawal when status is %s.",
                            status
                    )
            );

        }

        status = WithdrawalStatus.FAILED;
        failedAt = Instant.now();
        failureReason = reason;

    }

    public void cancel() {

        if (!isPending()) {

            throw new InvalidWithdrawalStateException(
                    String.format(
                            "Cannot cancel withdrawal when status is %s.",
                            status
                    )
            );

        }

        status = WithdrawalStatus.CANCELLED;
        cancelledAt = Instant.now();

    }

    /*
     * Helpers
     */

    private void validateAmount(
            BigDecimal amount
    ) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountException();

        }

    }

}