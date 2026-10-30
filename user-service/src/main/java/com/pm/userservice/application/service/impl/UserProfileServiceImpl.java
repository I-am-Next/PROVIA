package com.pm.userservice.application.service.impl;

import com.pm.userservice.api.dto.UserProfileResponse;
import com.pm.userservice.api.dto.UserProfileUpdateRequest;
import com.pm.userservice.application.service.UserProfileService;
import com.pm.userservice.domain.UserProfile;
import com.pm.userservice.exception.UserProfileNotFoundException;
import com.pm.userservice.infrastructure.persistence.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileServiceImpl(
            UserProfileRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(
            String authUserId
    ) {

        UserProfile profile = repository.findByAuthUserId(authUserId)
                .orElseThrow(UserProfileNotFoundException::new);

        return UserProfileResponse.from(profile);
    }

    @Override
    public UserProfileResponse updateMyProfile(
            String authUserId,
            UserProfileUpdateRequest request
    ) {

        UserProfile profile = repository.findByAuthUserId(authUserId)
                .orElseThrow(UserProfileNotFoundException::new);

        profile.updateProfile(
                request.firstName(),
                request.lastName(),
                request.phone()
        );

        return UserProfileResponse.from(profile);
    }
}