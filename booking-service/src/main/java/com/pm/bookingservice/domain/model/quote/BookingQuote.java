package com.pm.bookingservice.domain.model.quote;

import com.pm.bookingservice.domain.exception.InvalidQuoteStateException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "booking_quotes",
        indexes = {
                @Index(
                        name = "idx_quote_booking",
                        columnList = "bookingId"
                ),
                @Index(
                        name = "idx_quote_provider",
                        columnList = "providerId"
                )
        }
)
public class BookingQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID bookingId;

    @Column(nullable = false, updatable = false)
    private UUID providerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal labourAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal materialsAmount;

    @Column(nullable = false)
    private boolean materialsReleased;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal serviceFee;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(
            length = 2000,
            nullable = false
    )
    private String description;

    @Column(nullable = false)
    private Integer estimatedDurationDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Required by JPA.
     */
    protected BookingQuote() {
    }

    private BookingQuote(
            UUID bookingId,
            UUID providerId,
            BigDecimal labourAmount,
            BigDecimal materialsAmount,
            BigDecimal serviceFee,
            BigDecimal totalAmount,
            String description,
            Integer estimatedDurationDays,
            Instant expiresAt
    ) {

        this.bookingId = Objects.requireNonNull(
                bookingId,
                "bookingId must not be null"
        );

        this.providerId = Objects.requireNonNull(
                providerId,
                "providerId must not be null"
        );

        this.labourAmount = requireNonNegative(
                labourAmount,
                "labourAmount"
        );

        this.materialsAmount = requireNonNegative(
                materialsAmount,
                "materialsAmount"
        );

        this.serviceFee = requireNonNegative(
                serviceFee,
                "serviceFee"
        );

        this.totalAmount = validateTotalAmount(
                totalAmount,
                this.labourAmount,
                this.materialsAmount,
                this.serviceFee
        );

        this.description = requireText(
                description,
                "description"
        );

        this.estimatedDurationDays =
                requirePositiveDuration(
                        estimatedDurationDays
                );

        this.expiresAt = Objects.requireNonNull(
                expiresAt,
                "expiresAt must not be null"
        );

        this.materialsReleased = false;
        this.status = QuoteStatus.PENDING;
    }

    public static BookingQuote create(
            UUID bookingId,
            UUID providerId,
            BigDecimal labourAmount,
            BigDecimal materialsAmount,
            BigDecimal serviceFee,
            BigDecimal totalAmount,
            String description,
            Integer estimatedDurationDays,
            Instant expiresAt
    ) {

        return new BookingQuote(
                bookingId,
                providerId,
                labourAmount,
                materialsAmount,
                serviceFee,
                totalAmount,
                description,
                estimatedDurationDays,
                expiresAt
        );
    }

    @PrePersist
    void prePersist() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {

        updatedAt = Instant.now();
    }

    /*
     * Domain validation
     */

    private static BigDecimal requireNonNegative(
            BigDecimal value,
            String field
    ) {

        Objects.requireNonNull(
                value,
                field + " must not be null"
        );

        if (value.signum() < 0) {

            throw new IllegalArgumentException(
                    field + " must not be negative"
            );
        }

        return value;
    }

    private static BigDecimal validateTotalAmount(
            BigDecimal totalAmount,
            BigDecimal labourAmount,
            BigDecimal materialsAmount,
            BigDecimal serviceFee
    ) {

        Objects.requireNonNull(
                totalAmount,
                "totalAmount must not be null"
        );

        BigDecimal calculatedTotal =
                labourAmount
                        .add(materialsAmount)
                        .add(serviceFee);

        if (totalAmount.compareTo(calculatedTotal) != 0) {

            throw new IllegalArgumentException(
                    "totalAmount must equal " +
                            "labourAmount + materialsAmount + serviceFee"
            );
        }

        return totalAmount;
    }

    private static String requireText(
            String value,
            String field
    ) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value;
    }

    private static Integer requirePositiveDuration(
            Integer estimatedDurationDays
    ) {

        if (estimatedDurationDays == null
                || estimatedDurationDays <= 0) {

            throw new IllegalArgumentException(
                    "estimatedDurationDays must be greater than zero"
            );
        }

        return estimatedDurationDays;
    }

    private void requirePending() {

        if (status != QuoteStatus.PENDING) {

            throw new InvalidQuoteStateException(
                    String.format(
                            "Quote cannot be modified when status is %s",
                            status
                    )
            );
        }
    }

    /*
     * Quote lifecycle
     */

    public void accept() {

        requirePending();

        status = QuoteStatus.ACCEPTED;
    }

    public void reject() {

        requirePending();

        status = QuoteStatus.REJECTED;
    }

    public void expire() {

        requirePending();

        status = QuoteStatus.EXPIRED;
    }

    public boolean isExpired() {

        return expiresAt != null
                && !Instant.now().isBefore(expiresAt);
    }

    public void markMaterialsReleased() {

        if (materialsReleased) {

            throw new InvalidQuoteStateException(
                    "Materials have already been released"
            );
        }

        if (status != QuoteStatus.ACCEPTED) {

            throw new InvalidQuoteStateException(
                    String.format(
                            "Cannot release materials when quote status is %s",
                            status
                    )
            );
        }

        materialsReleased = true;
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

    public UUID getProviderId() {
        return providerId;
    }

    public BigDecimal getLabourAmount() {
        return labourAmount;
    }

    public BigDecimal getMaterialsAmount() {
        return materialsAmount;
    }

    public boolean isMaterialsReleased() {
        return materialsReleased;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getDescription() {
        return description;
    }

    public Integer getEstimatedDurationDays() {
        return estimatedDurationDays;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
