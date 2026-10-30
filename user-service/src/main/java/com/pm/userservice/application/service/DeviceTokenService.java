package com.pm.userservice.application.service;

import com.pm.userservice.api.dto.DeviceTokenRequest;

import java.util.List;
import java.util.UUID;

public interface DeviceTokenService {

    void registerDevice(
            UUID userId,
            DeviceTokenRequest request
    );

    void removeDevice(
            UUID userId,
            String deviceToken
    );

    List<String> getUserDeviceTokens(UUID userId);

    void cleanupOldTokens();
}