package com.pm.contracts.auth;

public record UserRegisteredEvent(
        String userId,
        String email,
        String role) {}
