package com.pm.userservice.controller;

import com.pm.userservice.api.dto.DeviceTokenRequest;
import com.pm.userservice.api.dto.UserProfileResponse;
import com.pm.userservice.api.dto.UserProfileUpdateRequest;
import com.pm.userservice.application.service.DeviceTokenService;
import com.pm.userservice.application.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final DeviceTokenService deviceTokenService;

    public UserProfileController(
            UserProfileService userProfileService,
            DeviceTokenService deviceTokenService
    ) {
        this.userProfileService = userProfileService;
        this.deviceTokenService = deviceTokenService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {

        String authUserId = jwt.getSubject();

        return ResponseEntity.ok(
                userProfileService.getMyProfile(authUserId)
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserProfileUpdateRequest request
    ) {

        String authUserId = jwt.getSubject();

        return ResponseEntity.ok(
                userProfileService.updateMyProfile(
                        authUserId,
                        request
                )
        );
    }

    @PostMapping("/me/devices")
    public ResponseEntity<Void> registerDevice(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DeviceTokenRequest request
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        deviceTokenService.registerDevice(
                userId,
                request
        );

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me/devices/{deviceToken}")
    public ResponseEntity<Void> removeDevice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String deviceToken
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());

        deviceTokenService.removeDevice(
                userId,
                deviceToken
        );

        return ResponseEntity.noContent().build();
    }
}