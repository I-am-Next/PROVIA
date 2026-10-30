package com.pm.authservice.dto.request;

public record LogoutRequest(
        String refreshToken) { }
