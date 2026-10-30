package com.pm.ratingservice.infrastructure.persistence;

import com.pm.ratingservice.domain.entity.Rating;
import com.pm.ratingservice.domain.enums.ReviewerRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RatingJpaRepository
        extends JpaRepository<Rating, UUID> {

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

    @Query("""
            SELECT
                AVG(r.score) AS averageRating,
                COUNT(r) AS totalRatings
            FROM Rating r
            WHERE r.revieweeUserId = :revieweeUserId
              AND r.revieweeRole = :revieweeRole
            """)
    RatingStatisticsProjection getRatingStatistics(
            @Param("revieweeUserId") UUID revieweeUserId,
            @Param("revieweeRole") ReviewerRole revieweeRole
    );
}