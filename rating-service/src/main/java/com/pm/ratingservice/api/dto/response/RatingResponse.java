package com.pm.ratingservice.api.dto.response;

import com.pm.ratingservice.domain.entity.Rating;
import com.pm.ratingservice.domain.enums.ReviewerRole;

import java.time.Instant;
import java.util.UUID;

public record RatingResponse(
        UUID id,
        UUID bookingId,
        UUID reviewerUserId,
        ReviewerRole reviewerRole,
        UUID revieweeUserId,
        ReviewerRole revieweeRole,
        int score,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {

    public static RatingResponse from(Rating rating) {

        return new RatingResponse(
                rating.getId(),
                rating.getBookingId(),
                rating.getReviewerUserId(),
                rating.getReviewerRole(),
                rating.getRevieweeUserId(),
                rating.getRevieweeRole(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}
