package com.pm.ratingservice.domain.entity;

import com.pm.ratingservice.domain.enums.ReviewerRole;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "ratings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rating_booking_reviewer_reviewee",
                        columnNames = {
                                "booking_id",
                                "reviewer_user_id",
                                "reviewee_user_id"
                        }
                )
        }
)
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID bookingId;

    @Column(nullable = false)
    private UUID reviewerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewerRole reviewerRole;

    @Column(nullable = false)
    private UUID revieweeUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewerRole revieweeRole;

    @Column(nullable = false)
    private int score;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Rating() {
    }

    private Rating(
            UUID bookingId,
            UUID reviewerUserId,
            ReviewerRole reviewerRole,
            UUID revieweeUserId,
            ReviewerRole revieweeRole,
            int score,
            String comment
    ) {
        this.bookingId = bookingId;
        this.reviewerUserId = reviewerUserId;
        this.reviewerRole = reviewerRole;
        this.revieweeUserId = revieweeUserId;
        this.revieweeRole = revieweeRole;
        this.score = score;
        this.comment = comment;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Rating create(
            UUID bookingId,
            UUID reviewerUserId,
            ReviewerRole reviewerRole,
            UUID revieweeUserId,
            ReviewerRole revieweeRole,
            int score,
            String comment
    ) {

        validateScore(score);

        return new Rating(
                bookingId,
                reviewerUserId,
                reviewerRole,
                revieweeUserId,
                revieweeRole,
                score,
                comment
        );
    }

    public void update(
            int score,
            String comment
    ) {

        validateScore(score);

        this.score = score;
        this.comment = comment;
        this.updatedAt = Instant.now();
    }

    private static void validateScore(int score) {

        if (score < 1 || score > 5) {
            throw new IllegalArgumentException(
                    "Rating score must be between 1 and 5"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getReviewerUserId() {
        return reviewerUserId;
    }

    public ReviewerRole getReviewerRole() {
        return reviewerRole;
    }

    public UUID getRevieweeUserId() {
        return revieweeUserId;
    }

    public ReviewerRole getRevieweeRole() {
        return revieweeRole;
    }

    public int getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}