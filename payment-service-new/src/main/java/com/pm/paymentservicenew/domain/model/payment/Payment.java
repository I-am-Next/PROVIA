package com.pm.paymentservicenew.domain.model.payment;

import com.pm.paymentservicenew.domain.exception.InvalidPaymentStateException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_customer", columnList = "customerId"),
                @Index(name = "idx_payment_provider", columnList = "providerId"),
                @Index(name = "idx_payment_status", columnList = "status")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID bookingId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID providerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, length = 3)
    private String currency;

    // Nullable until the gateway initialization succeeds.
    @Column(unique = true)
    private String gatewayReference;

    private Instant gatewayPaidAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant completedAt;

    private Instant failedAt;

    protected Payment() {
    }

    private Payment(
            UUID bookingId,
            UUID customerId,
            UUID providerId,
            BigDecimal amount,
            String currency
    ) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.providerId = providerId;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public static Payment create(
            UUID bookingId,
            UUID customerId,
            UUID providerId,
            BigDecimal amount
    ) {
        return new Payment(
                bookingId,
                customerId,
                providerId,
                amount,
                "NGN"
        );
    }

    /*
     * Getters
     */

    public UUID getId() {
        return id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public String getGatewayReference() {
        return gatewayReference;
    }

    public Instant getGatewayPaidAt() {
        return gatewayPaidAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    /*
     * State helpers
     */

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public boolean isCancelled() {
        return status == PaymentStatus.CANCELLED;
    }

    public boolean isRefunded() {
        return status == PaymentStatus.REFUNDED;
    }

    /*
     * State transitions
     */

    public void assignGatewayReference(String gatewayReference) {

        if (!isPending()) {
            throw new InvalidPaymentStateException(
                    String.format(
                            "Cannot assign gateway reference when payment status is %s.",
                            status
                    )
            );
        }

        this.gatewayReference = gatewayReference;
    }

    public void complete(Instant gatewayPaidAt) {

        if (!isPending()) {
            throw new InvalidPaymentStateException(
                    String.format(
                            "Cannot complete payment when status is %s.",
                            status
                    )
            );
        }

        if (gatewayReference == null || gatewayReference.isBlank()) {
            throw new InvalidPaymentStateException(
                    "Cannot complete payment without a gateway reference."
            );
        }

        this.gatewayPaidAt = gatewayPaidAt;
        this.completedAt = Instant.now();
        this.status = PaymentStatus.COMPLETED;
    }

    public void fail() {

        if (!isPending()) {
            throw new InvalidPaymentStateException(
                    String.format(
                            "Cannot fail payment when status is %s.",
                            status
                    )
            );
        }

        this.failedAt = Instant.now();
        this.status = PaymentStatus.FAILED;
    }

    public void cancel() {

        if (!isPending()) {
            throw new InvalidPaymentStateException(
                    String.format(
                            "Cannot cancel payment when status is %s.",
                            status
                    )
            );
        }

        this.status = PaymentStatus.CANCELLED;
    }

    public void refund() {

        if (!isCompleted()) {
            throw new InvalidPaymentStateException(
                    String.format(
                            "Cannot refund payment when status is %s.",
                            status
                    )
            );
        }

        this.status = PaymentStatus.REFUNDED;
    }
}