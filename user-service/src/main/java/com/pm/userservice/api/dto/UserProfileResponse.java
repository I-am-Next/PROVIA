package com.pm.userservice.api.dto;

import com.pm.userservice.domain.UserProfile;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String authUserId,
        String email,
        String firstName,
        String lastName,
        String phone,
        Instant createdAt
) {

    public static UserProfileResponse from(
            UserProfile profile
    ) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getAuthUserId(),
                profile.getEmail(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getCreatedAt()
        );
    }
}