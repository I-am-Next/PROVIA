package com.pm.providerservice.application.service;

import com.pm.providerservice.api.dto.OwnerProviderProfileResponse;
import com.pm.providerservice.api.dto.ProviderProfileCreateRequest;
import com.pm.providerservice.api.dto.ProviderProfileUpdateRequest;
import com.pm.providerservice.api.dto.PublicProviderProfileResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

public interface ProviderProfileService {

    OwnerProviderProfileResponse getMyProfile(
            Jwt jwt
    );

    OwnerProviderProfileResponse createMyProfile(
            Jwt jwt,
            ProviderProfileCreateRequest request
    );

    OwnerProviderProfileResponse updateMyProfile(
            Jwt jwt,
            ProviderProfileUpdateRequest request
    );

    OwnerProviderProfileResponse updateProfileDetails(
            Jwt jwt,
            ProviderProfileUpdateRequest request
    );

    OwnerProviderProfileResponse updateProfilePhoto(
            Jwt jwt,
            UUID mediaId
    );

    List<PublicProviderProfileResponse> getPublicProvidersByCategory(
            String category
    );

    PublicProviderProfileResponse getProvider(
            UUID id
    );
}