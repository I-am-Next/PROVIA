package com.pm.ratingservice.api.dto.response;

import java.util.UUID;

public record ProviderRatingSummaryResponse(
        UUID providerId,
        double averageRating,
        long totalRatings
) {
}