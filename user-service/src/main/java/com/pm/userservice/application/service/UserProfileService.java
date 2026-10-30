package com.pm.userservice.application.service;

import com.pm.userservice.api.dto.UserProfileResponse;
import com.pm.userservice.api.dto.UserProfileUpdateRequest;

public interface UserProfileService {

    UserProfileResponse getMyProfile(String authUserId);

    UserProfileResponse updateMyProfile(
            String authUserId,
            UserProfileUpdateRequest request
    );
}