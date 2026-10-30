package com.pm.ratingservice.application.service;

import com.pm.ratingservice.api.dto.request.CreateRatingRequest;
import com.pm.ratingservice.api.dto.request.UpdateRatingRequest;
import com.pm.ratingservice.api.dto.response.ProviderRatingSummaryResponse;
import com.pm.ratingservice.api.dto.response.ProviderReviewsResponse;
import com.pm.ratingservice.api.dto.response.RatingResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RatingService {

    RatingResponse createRating(
            CreateRatingRequest request
    );

    RatingResponse getRating(
            UUID ratingId
    );

    RatingResponse updateRating(
            UUID ratingId,
            UpdateRatingRequest request
    );

    void deleteRating(UUID ratingId);

    ProviderRatingSummaryResponse getProviderRatingSummary(UUID providerId
    );

    ProviderReviewsResponse getProviderReviews(
            UUID providerId,
            Pageable pageable
    );
    RatingResponse getRatingByBookingId(
            UUID bookingId
    );
}