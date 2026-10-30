package com.pm.bookingservice.domain.model;

import com.pm.bookingservice.domain.exception.InvalidBookingStateException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "bookings",
        indexes = {

                @Index(
                        name = "idx_booking_user",
                        columnList = "userId"
                ),

                @Index(
                        name = "idx_booking_provider",
                        columnList = "providerUserId"
                ),

                @Index(
                        name = "idx_booking_status",
                        columnList = "status"
                ),

                @Index(
                        name = "idx_booking_provider_status",
                        columnList = "providerUserId,status"
                )

        }
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Customer Auth ID.
     */
    @Column(nullable = false, updatable = false)
    private UUID userId;

    /**
     * Provider Auth ID.
     */
    @Column(nullable = false, updatable = false)
    private UUID providerUserId;

    /**
     * Provider Service Aggregate ID.
     */
    @Column(nullable = false, updatable = false)
    private UUID providerServiceId;

    /**
     * Snapshot of the service name at booking creation.
     */
    @Column(nullable = false, updatable = false)
    private String serviceName;

    /**
     * Agreed booking amount.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(length = 1000)
    private String description;

    @Column(length = 1000)
    private String notes;

    @Embedded
    private BookingAddress address;

    @Column(nullable = false)
    private LocalDateTime scheduledStart;

    @Column(nullable = false)
    private LocalDateTime scheduledEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private LocalDateTime acceptedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    /**
     * Required by JPA.
     */
    protected Booking() {
    }

    private Booking(
            UUID userId,
            UUID providerUserId,
            UUID providerServiceId,
            String serviceName,
            BigDecimal price,
            String description,
            String notes,
            BookingAddress address,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        validateIdentity(
                userId,
                providerUserId,
                providerServiceId
        );

        this.serviceName = requireText(
                serviceName,
                "serviceName"
        );

        this.price = requireNonNegative(
                price,
                "price"
        );

        this.address = Objects.requireNonNull(
                address,
                "address must not be null"
        );

        validateSchedule(
                scheduledStart,
                scheduledEnd
        );

        this.userId = userId;
        this.providerUserId = providerUserId;
        this.providerServiceId = providerServiceId;
        this.description = description;
        this.notes = notes;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.status = BookingStatus.REQUESTED;
    }

    public static Booking create(
            UUID userId,
            UUID providerUserId,
            UUID providerServiceId,
            String serviceName,
            BigDecimal price,
            String description,
            String notes,
            BookingAddress address,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        return new Booking(
                userId,
                providerUserId,
                providerServiceId,
                serviceName,
                price,
                description,
                notes,
                address,
                scheduledStart,
                scheduledEnd
        );
    }

    @PrePersist
    private void prePersist() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {

        updatedAt = Instant.now();
    }

    /*
     * Domain validation
     */

    private static void validateIdentity(
            UUID userId,
            UUID providerUserId,
            UUID providerServiceId
    ) {

        Objects.requireNonNull(
                userId,
                "userId must not be null"
        );

        Objects.requireNonNull(
                providerUserId,
                "providerUserId must not be null"
        );

        Objects.requireNonNull(
                providerServiceId,
                "providerServiceId must not be null"
        );
    }

    private static void validateSchedule(
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        Objects.requireNonNull(
                scheduledStart,
                "scheduledStart must not be null"
        );

        Objects.requireNonNull(
                scheduledEnd,
                "scheduledEnd must not be null"
        );

        if (!scheduledEnd.isAfter(scheduledStart)) {

            throw new IllegalArgumentException(
                    "Scheduled end must be after scheduled start"
            );
        }
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

    private void requireStatus(
            BookingStatus... allowedStatuses
    ) {

        for (BookingStatus allowedStatus : allowedStatuses) {

            if (status == allowedStatus) {
                return;
            }
        }

        throw new InvalidBookingStateException(
                String.format(
                        "Operation not allowed when booking is in status %s",
                        status
                )
        );
    }

    /*
     * Getters
     */

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getProviderUserId() {
        return providerUserId;
    }

    public UUID getProviderServiceId() {
        return providerServiceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getNotes() {
        return notes;
    }

    public BookingAddress getAddress() {
        return address;
    }

    public LocalDateTime getScheduledStart() {
        return scheduledStart;
    }

    public LocalDateTime getScheduledEnd() {
        return scheduledEnd;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    /*
     * Booking lifecycle
     */

    public void accept() {

        requireStatus(BookingStatus.REQUESTED);

        status = BookingStatus.AWAITING_QUOTE;
        acceptedAt = LocalDateTime.now();
    }

    public void reject() {

        requireStatus(BookingStatus.REQUESTED);

        status = BookingStatus.REJECTED;
    }

    public void quoteSent() {

        requireStatus(BookingStatus.AWAITING_QUOTE);

        status = BookingStatus.QUOTE_SENT;
    }

    public void quoteAccepted(
            BigDecimal quotedPrice
    ) {

        requireStatus(BookingStatus.QUOTE_SENT);

        this.price = requireNonNegative(
                quotedPrice,
                "quotedPrice"
        );

        status = BookingStatus.QUOTE_ACCEPTED;
    }

    public void quoteRejected() {

        requireStatus(BookingStatus.QUOTE_SENT);

        status = BookingStatus.QUOTE_REJECTED;
    }

    public void paymentCompleted() {

        requireStatus(BookingStatus.QUOTE_ACCEPTED);

        status = BookingStatus.PAYMENT_COMPLETED;
    }

    public void materialsReleased() {

        requireStatus(BookingStatus.PAYMENT_COMPLETED);

        status = BookingStatus.MATERIALS_RELEASED;
    }

    public void start() {

        requireStatus(BookingStatus.MATERIALS_RELEASED);

        status = BookingStatus.IN_PROGRESS;
        startedAt = LocalDateTime.now();
    }

    public void complete() {

        requireStatus(BookingStatus.IN_PROGRESS);

        status = BookingStatus.COMPLETED;
        completedAt = LocalDateTime.now();
    }

    public void confirmCompletion() {

        requireStatus(BookingStatus.COMPLETED);

        status = BookingStatus.COMPLETION_CONFIRMED;
    }

    public void cancel() {

        requireStatus(
                BookingStatus.REQUESTED,
                BookingStatus.AWAITING_QUOTE,
                BookingStatus.QUOTE_SENT,
                BookingStatus.QUOTE_ACCEPTED
        );

        status = BookingStatus.CANCELLED;
        cancelledAt = LocalDateTime.now();
    }

    public void update(
            String description,
            String notes,
            BookingAddress address,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        requireStatus(
                BookingStatus.REQUESTED,
                BookingStatus.AWAITING_QUOTE
        );

        Objects.requireNonNull(
                address,
                "address must not be null"
        );

        validateSchedule(
                scheduledStart,
                scheduledEnd
        );

        this.description = description;
        this.notes = notes;
        this.address = address;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
    }
}