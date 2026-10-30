package com.pm.userservice.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(

        @NotBlank(message = "Device token is required")
        String deviceToken,

        @NotBlank(message = "Device type is required")
        String deviceType
) {
}