
package com.pm.paymentservicenew.domain.model.escrow;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "escrow_releases",
        indexes = {
                @Index(
                        name = "idx_escrow_release_escrow",
                        columnList = "escrowId"
                ),
                @Index(
                        name = "idx_escrow_release_booking",
                        columnList = "bookingId"
                ),
                @Index(
                        name = "idx_escrow_release_provider",
                        columnList = "providerId"
                ),
                @Index(
                        name = "idx_escrow_release_occurred_at",
                        columnList = "releasedAt"
                )
        }
)
public class EscrowRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID escrowId;

    @Column(nullable = false)
    private UUID bookingId;

    @Column(nullable = true)
    private UUID quoteId;

    @Column(nullable = false)
    private UUID providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscrowReleaseType releaseType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant releasedAt;

    protected EscrowRelease() {
    }

    private EscrowRelease(
            UUID escrowId,
            UUID bookingId,
            UUID quoteId,
            UUID providerId,
            EscrowReleaseType releaseType,
            BigDecimal amount,
            String description
    ) {
        this.escrowId = escrowId;
        this.bookingId = bookingId;
        this.quoteId = quoteId;
        this.providerId = providerId;
        this.releaseType = releaseType;
        this.amount = amount;
        this.description = description;
        this.releasedAt = Instant.now();
    }

    public static EscrowRelease create(
            UUID escrowId,
            UUID bookingId,
            UUID quoteId,
            UUID providerId,
            EscrowReleaseType releaseType,
            BigDecimal amount,
            String description
    ) {
        return new EscrowRelease(
                escrowId,
                bookingId,
                quoteId,
                providerId,
                releaseType,
                amount,
                description
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getEscrowId() {
        return escrowId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getQuoteId() {
        return quoteId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public EscrowReleaseType getReleaseType() {
        return releaseType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }
}
