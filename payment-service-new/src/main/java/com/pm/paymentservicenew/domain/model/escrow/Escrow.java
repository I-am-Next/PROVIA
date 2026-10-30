package com.pm.paymentservicenew.domain.model.escrow;

import com.pm.paymentservicenew.domain.exception.EscrowOverReleaseException;
import com.pm.paymentservicenew.domain.exception.InvalidEscrowStateException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "escrows",
        indexes = {
                @Index(name = "idx_escrow_booking", columnList = "bookingId"),
                @Index(name = "idx_escrow_customer", columnList = "customerId"),
                @Index(name = "idx_escrow_provider", columnList = "providerId"),
                @Index(name = "idx_escrow_status", columnList = "status")
        }
)
public class Escrow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID paymentId;

    @Column(nullable = false, unique = true)
    private UUID bookingId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID providerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal releasedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscrowStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant fullyReleasedAt;

    private Instant refundedAt;

    protected Escrow() {
    }

    private Escrow(
            UUID paymentId,
            UUID bookingId,
            UUID customerId,
            UUID providerId,
            BigDecimal totalAmount
    ) {

        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.providerId = providerId;
        this.totalAmount = totalAmount;
        this.releasedAmount = BigDecimal.ZERO;
        this.status = EscrowStatus.HOLDING;
        this.createdAt = Instant.now();

    }

    public static Escrow create(
            UUID paymentId,
            UUID bookingId,
            UUID customerId,
            UUID providerId,
            BigDecimal totalAmount
    ) {

        return new Escrow(
                paymentId,
                bookingId,
                customerId,
                providerId,
                totalAmount
        );

    }

    /*
     * Getters
     */

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getReleasedAmount() {
        return releasedAmount;
    }

    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(releasedAmount);
    }

    public EscrowStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getFullyReleasedAt() {
        return fullyReleasedAt;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    /*
     * State helpers
     */

    public boolean isHolding() {
        return status == EscrowStatus.HOLDING;
    }

    public boolean isPartiallyReleased() {
        return status == EscrowStatus.PARTIALLY_RELEASED;
    }

    public boolean isFullyReleased() {
        return status == EscrowStatus.FULLY_RELEASED;
    }

    public boolean isRefunded() {
        return status == EscrowStatus.REFUNDED;
    }

    /*
     * State transitions
     */

    public void release(
            BigDecimal amount,
            EscrowReleaseType releaseType
    ) {

        if (isRefunded()) {

            throw new InvalidEscrowStateException(

                    String.format(
                            "Cannot release escrow when status is %s.",
                            status
                    )

            );

        }

        if (isFullyReleased()) {

            throw new InvalidEscrowStateException(

                    String.format(
                            "Cannot release escrow when status is %s.",
                            status
                    )

            );

        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidEscrowStateException(
                    "Release amount must be greater than zero."
            );

        }

        if (releasedAmount.add(amount).compareTo(totalAmount) > 0) {

            throw new EscrowOverReleaseException(
                    "Release amount exceeds the remaining escrow balance."
            );

        }

        releasedAmount = releasedAmount.add(amount);

        /*
         * TODO:
         * releaseType will be used later when
         * implementing escrow release history/audit records.
         */

        if (releasedAmount.compareTo(totalAmount) == 0) {

            status = EscrowStatus.FULLY_RELEASED;
            fullyReleasedAt = Instant.now();

        } else {

            status = EscrowStatus.PARTIALLY_RELEASED;

        }

    }

    public void refund() {

        if (isFullyReleased()) {

            throw new InvalidEscrowStateException(

                    String.format(
                            "Cannot refund escrow when status is %s.",
                            status
                    )

            );

        }

        if (isRefunded()) {

            throw new InvalidEscrowStateException(

                    String.format(
                            "Cannot refund escrow when status is %s.",
                            status
                    )

            );

        }

        status = EscrowStatus.REFUNDED;
        refundedAt = Instant.now();

    }

}