package com.pm.userservice.api.dto;

public record DeviceTokenResponse(
        String deviceToken,
        String deviceType
) {
}