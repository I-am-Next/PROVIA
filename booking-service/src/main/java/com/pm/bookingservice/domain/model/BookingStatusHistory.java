package com.pm.bookingservice.domain.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "booking_status_history",
        indexes = {

                @Index(
                        name = "idx_booking_history_booking",
                        columnList = "booking_id"
                ),

                @Index(
                        name = "idx_booking_history_changed_at",
                        columnList = "changed_at"
                )

        }
)
public class BookingStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "booking_id",
            nullable = false,
            updatable = false
    )
    private UUID bookingId;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            updatable = false
    )
    private BookingStatus status;

    @Column(
            name = "changed_at",
            nullable = false,
            updatable = false
    )
    private Instant changedAt;

    @Column(
            name = "changed_by",
            nullable = false,
            updatable = false
    )
    private UUID changedBy;

    @Column(
            length = 500,
            updatable = false
    )
    private String note;

    protected BookingStatusHistory() {
        // JPA
    }

    private BookingStatusHistory(
            UUID bookingId,
            BookingStatus status,
            Instant changedAt,
            UUID changedBy,
            String note
    ) {

        this.bookingId = bookingId;

        this.status = status;

        this.changedAt = changedAt;

        this.changedBy = changedBy;

        this.note = note;
    }

    public static BookingStatusHistory create(
            UUID bookingId,
            BookingStatus status,
            UUID changedBy,
            String note
    ) {

        return new BookingStatusHistory(

                bookingId,

                status,

                Instant.now(),

                changedBy,

                note

        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public String getNote() {
        return note;
    }
}