package com.pm.paymentservicenew.domain.model.wallet;

import com.pm.paymentservicenew.domain.exception.InsufficientWalletBalanceException;
import com.pm.paymentservicenew.domain.exception.InvalidAmountException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true)
    private UUID providerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal pendingBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal reservedBalance;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Wallet() {
    }

    private Wallet(UUID providerId) {

        this.providerId = providerId;
        this.availableBalance = BigDecimal.ZERO;
        this.pendingBalance = BigDecimal.ZERO;
        this.reservedBalance = BigDecimal.ZERO;
        this.createdAt = Instant.now();

    }

    public static Wallet create(UUID providerId) {

        return new Wallet(providerId);

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

    public UUID getProviderId() {
        return providerId;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getPendingBalance() {
        return pendingBalance;
    }

    public BigDecimal getReservedBalance() {
        return reservedBalance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getTotalBalance() {

        return availableBalance
                .add(reservedBalance)
                .add(pendingBalance);

    }

    /*
     * Helpers
     */

    public boolean hasAvailableFunds(BigDecimal amount) {

        return availableBalance.compareTo(amount) >= 0;

    }

    public boolean hasReservedFunds(BigDecimal amount) {

        return reservedBalance.compareTo(amount) >= 0;

    }

    public boolean hasPendingFunds(BigDecimal amount) {

        return pendingBalance.compareTo(amount) >= 0;

    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountException();

        }

    }

    /*
     * Domain operations
     */

    public void credit(BigDecimal amount) {

        validateAmount(amount);

        availableBalance = availableBalance.add(amount);

    }

    public void debit(BigDecimal amount) {

        validateAmount(amount);

        if (!hasAvailableFunds(amount)) {

            throw new InsufficientWalletBalanceException(
                    "Available wallet balance is insufficient."
            );

        }

        availableBalance = availableBalance.subtract(amount);

    }

    public void reserve(BigDecimal amount) {

        validateAmount(amount);

        if (!hasAvailableFunds(amount)) {

            throw new InsufficientWalletBalanceException(
                    "Available wallet balance is insufficient."
            );

        }

        availableBalance = availableBalance.subtract(amount);
        reservedBalance = reservedBalance.add(amount);

    }

    public void releaseReserved(BigDecimal amount) {

        validateAmount(amount);

        if (!hasReservedFunds(amount)) {

            throw new InsufficientWalletBalanceException(
                    "Reserved wallet balance is insufficient."
            );

        }

        reservedBalance = reservedBalance.subtract(amount);
        availableBalance = availableBalance.add(amount);

    }

    /**
     * Called when a withdrawal succeeds.
     * Money leaves the platform.
     */
    public void withdrawReserved(BigDecimal amount) {

        validateAmount(amount);

        if (!hasReservedFunds(amount)) {

            throw new InsufficientWalletBalanceException(
                    "Reserved wallet balance is insufficient."
            );

        }

        reservedBalance = reservedBalance.subtract(amount);

    }

    public void addPending(BigDecimal amount) {

        validateAmount(amount);

        pendingBalance = pendingBalance.add(amount);

    }

    public void releasePending(BigDecimal amount) {

        validateAmount(amount);

        if (!hasPendingFunds(amount)) {

            throw new InsufficientWalletBalanceException(
                    "Pending wallet balance is insufficient."
            );

        }

        pendingBalance = pendingBalance.subtract(amount);
        availableBalance = availableBalance.add(amount);

    }

}