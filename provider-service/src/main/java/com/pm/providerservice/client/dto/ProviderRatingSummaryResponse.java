package com.pm.providerservice.client.dto;

import java.util.UUID;

public record ProviderRatingSummaryResponse(
        UUID providerId,
        double averageRating,
        long totalRatings
) {
}