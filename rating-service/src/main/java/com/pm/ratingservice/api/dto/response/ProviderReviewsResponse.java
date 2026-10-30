package com.pm.ratingservice.api.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public record ProviderReviewsResponse(
        UUID providerId,
        double averageRating,
        long totalRatings,
        List<RatingResponse> content,
        int page,
        int size,
        int totalPages,
        long totalElements
) {

    public static ProviderReviewsResponse from(
            UUID providerId,
            double averageRating,
            long totalRatings,
            Page<RatingResponse> reviews
    ) {

        return new ProviderReviewsResponse(
                providerId,
                averageRating,
                totalRatings,
                reviews.getContent(),
                reviews.getNumber(),
                reviews.getSize(),
                reviews.getTotalPages(),
                reviews.getTotalElements()
        );
    }
}
