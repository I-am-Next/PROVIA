package com.pm.providerservice.api.dto;

import com.pm.providerservice.domain.ProviderProfile;

import java.util.UUID;

public record PublicProviderProfileResponse(

        UUID id,

        String businessName,

        String category,

        String description,

        boolean verified,

        double averageRating,

        int ratingCount,

        UUID profilePhotoMediaId

) {

    public static PublicProviderProfileResponse from(
            ProviderProfile profile
    ) {

        return new PublicProviderProfileResponse(
                profile.getId(),
                profile.getBusinessName(),
                profile.getCategory(),
                profile.getDescription(),
                profile.isVerified(),
                profile.getAverageRating(),
                profile.getRatingCount(),
                profile.getProfilePhotoMediaId()
        );
    }
}