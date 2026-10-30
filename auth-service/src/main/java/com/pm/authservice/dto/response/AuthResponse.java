package com.pm.authservice.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken
) { }
