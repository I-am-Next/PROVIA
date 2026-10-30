package com.pm.userservice.api.dto;

public record UserProfileUpdateRequest(
        String firstName,
        String lastName,
        String phone
) {
}