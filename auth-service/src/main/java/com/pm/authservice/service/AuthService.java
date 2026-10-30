package com.pm.authservice.service;

import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.RegisterUserRequest;
import com.pm.authservice.dto.response.AuthResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse registerUser (RegisterUserRequest request);

    AuthResponse loginUser(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
    void logoutAll(UUID userId);
}
