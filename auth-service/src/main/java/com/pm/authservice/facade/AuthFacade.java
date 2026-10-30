package com.pm.authservice.facade;

import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.RegisterUserRequest;
import com.pm.authservice.dto.response.AuthResponse;
import com.pm.authservice.service.AuthService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthFacade {

    private final AuthService authService;

    public AuthFacade(AuthService authService) {
        this.authService = authService;
    }

    public AuthResponse register(RegisterUserRequest request) {
        return authService.registerUser(request);
    }

    public AuthResponse login(LoginRequest request) {
        return authService.loginUser(request);
    }

    public AuthResponse refresh(String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    public void logout(String refreshToken) {
        authService.logout(refreshToken);
    }

    public void logoutAll(UUID userId) {
        authService.logoutAll(userId);
    }
}
