package com.pm.providerservice.api.dto;

import com.pm.providerservice.client.dto.ProviderRatingSummaryResponse;
import com.pm.providerservice.domain.ProviderProfile;

import java.time.Instant;
import java.util.UUID;

public record OwnerProviderProfileResponse(

        String businessName,

        String email,

        String category,

        String description,

        String phone,

        boolean verified,

        double averageRating,

        int ratingCount,

        UUID profilePhotoMediaId,

        Instant createdAt

) {

    public static OwnerProviderProfileResponse from(
            ProviderProfile profile
    ) {

        return new OwnerProviderProfileResponse(
                profile.getBusinessName(),
                profile.getEmail(),
                profile.getCategory(),
                profile.getDescription(),
                profile.getPhone(),
                profile.isVerified(),
                profile.getAverageRating(),
                profile.getRatingCount(),
                profile.getProfilePhotoMediaId(),
                profile.getCreatedAt()
        );
    }

    public static OwnerProviderProfileResponse from(
            ProviderProfile profile,
            ProviderRatingSummaryResponse summary
    ) {

        return new OwnerProviderProfileResponse(
                profile.getBusinessName(),
                profile.getEmail(),
                profile.getCategory(),
                profile.getDescription(),
                profile.getPhone(),
                profile.isVerified(),
                summary.averageRating(),
                (int) summary.totalRatings(),
                profile.getProfilePhotoMediaId(),
                profile.getCreatedAt()
        );
    }
}