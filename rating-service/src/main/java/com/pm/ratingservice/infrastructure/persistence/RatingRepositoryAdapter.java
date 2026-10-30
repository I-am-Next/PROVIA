package com.pm.ratingservice.infrastructure.persistence;

import com.pm.ratingservice.application.port.RatingRepository;
import com.pm.ratingservice.domain.entity.Rating;
import com.pm.ratingservice.domain.enums.ReviewerRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RatingRepositoryAdapter
        implements RatingRepository {

    private final RatingJpaRepository repository;

    public RatingRepositoryAdapter(
            RatingJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Rating save(Rating rating) {
        return repository.save(rating);
    }

    @Override
    public Optional<Rating> findById(UUID ratingId) {
        return repository.findById(ratingId);
    }

    @Override
    public Optional<Rating> findByBookingIdAndReviewerUserId(
            UUID bookingId,
            UUID reviewerUserId
    ) {

        return repository.findByBookingIdAndReviewerUserId(
                bookingId,
                reviewerUserId
        );
    }

    @Override
    public boolean existsByBookingIdAndReviewerUserIdAndRevieweeUserId(
            UUID bookingId,
            UUID reviewerUserId,
            UUID revieweeUserId
    ) {

        return repository
                .existsByBookingIdAndReviewerUserIdAndRevieweeUserId(
                        bookingId,
                        reviewerUserId,
                        revieweeUserId
                );
    }

    @Override
    public Page<Rating> findByReviewerUserId(
            UUID reviewerUserId,
            Pageable pageable
    ) {

        return repository.findByReviewerUserId(
                reviewerUserId,
                pageable
        );
    }

    @Override
    public Page<Rating> findByRevieweeUserIdAndRevieweeRole(
            UUID revieweeUserId,
            ReviewerRole revieweeRole,
            Pageable pageable
    ) {

        return repository.findByRevieweeUserIdAndRevieweeRole(
                revieweeUserId,
                revieweeRole,
                pageable
        );
    }

    @Override
    public RatingStatistics getStatistics(
            UUID revieweeUserId,
            ReviewerRole revieweeRole
    ) {

        RatingStatisticsProjection result =
                repository.getRatingStatistics(
                        revieweeUserId,
                        revieweeRole
                );

        if (result == null) {
            return new RatingStatistics(
                    0.0,
                    0L
            );
        }

        double averageRating =
                result.getAverageRating() != null
                        ? result.getAverageRating()
                        : 0.0;

        long totalRatings =
                result.getTotalRatings() != null
                        ? result.getTotalRatings()
                        : 0L;

        return new RatingStatistics(
                averageRating,
                totalRatings
        );
    }
    @Override
    public void delete(Rating rating) {
        repository.delete(rating);
    }
}