package com.pm.ratingservice.application.port;

import com.pm.ratingservice.domain.entity.Rating;
import com.pm.ratingservice.domain.enums.ReviewerRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface RatingRepository {

    Rating save(Rating rating);

    Optional<Rating> findById(UUID ratingId);

    Optional<Rating> findByBookingIdAndReviewerUserId(
            UUID bookingId,
            UUID reviewerUserId
    );

    boolean existsByBookingIdAndReviewerUserIdAndRevieweeUserId(
            UUID bookingId,
            UUID reviewerUserId,
            UUID revieweeUserId
    );

    Page<Rating> findByReviewerUserId(
            UUID reviewerUserId,
            Pageable pageable
    );

    Page<Rating> findByRevieweeUserIdAndRevieweeRole(
            UUID revieweeUserId,
            ReviewerRole revieweeRole,
            Pageable pageable
    );

    RatingStatistics getStatistics(
            UUID revieweeUserId,
            ReviewerRole revieweeRole
    );

    void delete(Rating rating);

    record RatingStatistics(
            double averageRating,
            long totalRatings
    ) {
    }
}