package com.pm.providerservice.application.mapper;

import com.pm.contracts.provider.ProviderCreatedEvent;
import com.pm.contracts.provider.ProviderUpdatedEvent;
import com.pm.providerservice.domain.ProviderProfile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ProviderEventMapper {

    public ProviderCreatedEvent toCreatedEvent(
            ProviderProfile profile
    ) {

        return new ProviderCreatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                profile.getId(),
                profile.getAuthUserId(),
                profile.getBusinessName(),
                profile.getEmail(),
                profile.getCategory(),
                profile.getDescription(),
                profile.getPhone(),
                profile.isVerified(),
                profile.getProfilePhotoMediaId()
        );
    }

    public ProviderUpdatedEvent toUpdatedEvent(
            ProviderProfile profile
    ) {

        return new ProviderUpdatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                profile.getId(),
                profile.getAuthUserId(),
                profile.getBusinessName(),
                profile.getEmail(),
                profile.getCategory(),
                profile.getDescription(),
                profile.getPhone(),
                profile.isVerified(),
                profile.getProfilePhotoMediaId()
        );
    }
}