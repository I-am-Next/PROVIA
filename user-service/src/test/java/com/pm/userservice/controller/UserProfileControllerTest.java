package com.pm.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.userservice.api.dto.DeviceTokenRequest;
import com.pm.userservice.api.dto.UserProfileResponse;
import com.pm.userservice.api.dto.UserProfileUpdateRequest;
import com.pm.userservice.application.service.DeviceTokenService;
import com.pm.userservice.application.service.UserProfileService;
import com.pm.userservice.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
@Import(SecurityConfig.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private DeviceTokenService deviceTokenService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final Instant ISSUED_AT =
            Instant.parse("2026-01-01T00:00:00Z");

    private static final Instant EXPIRES_AT =
            Instant.parse("2026-01-01T01:00:00Z");

    private RequestPostProcessor authenticatedAs(String subject) {
        return jwt().jwt(jwt -> {
            jwt.tokenValue("test-token");
            jwt.subject(subject);
            jwt.issuedAt(ISSUED_AT);
            jwt.expiresAt(EXPIRES_AT);
        });
    }

    @Test
    void getMyProfile_usesJwtSubject() throws Exception {

        String authUserId = "auth-user-123";

        UserProfileResponse response =
                new UserProfileResponse(
                        1L,
                        authUserId,
                        "user@example.com",
                        "Emmanuel",
                        "Nde",
                        "08012345678",
                        Instant.parse("2026-01-01T00:00:00Z")
                );

        when(userProfileService.getMyProfile(authUserId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/users/me")
                                .with(authenticatedAs(authUserId))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.authUserId")
                                .value(authUserId)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("user@example.com")
                )
                .andExpect(
                        jsonPath("$.firstName")
                                .value("Emmanuel")
                )
                .andExpect(
                        jsonPath("$.lastName")
                                .value("Nde")
                )
                .andExpect(
                        jsonPath("$.phone")
                                .value("08012345678")
                );

        verify(userProfileService)
                .getMyProfile(authUserId);
    }

    @Test
    void updateMyProfile_usesJwtSubject() throws Exception {

        String authUserId = "auth-user-123";

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(
                        "Emmanuel",
                        "Nde",
                        "08012345678"
                );

        UserProfileResponse response =
                new UserProfileResponse(
                        1L,
                        authUserId,
                        "user@example.com",
                        "Emmanuel",
                        "Nde",
                        "08012345678",
                        Instant.parse("2026-01-01T00:00:00Z")
                );

        when(
                userProfileService.updateMyProfile(
                        eq(authUserId),
                        any(UserProfileUpdateRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/users/me")
                                .with(authenticatedAs(authUserId))
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.authUserId")
                                .value(authUserId)
                )
                .andExpect(
                        jsonPath("$.firstName")
                                .value("Emmanuel")
                )
                .andExpect(
                        jsonPath("$.lastName")
                                .value("Nde")
                )
                .andExpect(
                        jsonPath("$.phone")
                                .value("08012345678")
                );

        verify(userProfileService)
                .updateMyProfile(
                        eq(authUserId),
                        any(UserProfileUpdateRequest.class)
                );
    }

    @Test
    void registerDevice_usesJwtUserId() throws Exception {

        UUID userId = UUID.randomUUID();

        DeviceTokenRequest request =
                new DeviceTokenRequest(
                        "device-token",
                        "ANDROID"
                );

        mockMvc.perform(
                        post("/api/v1/users/me/devices")
                                .with(
                                        authenticatedAs(
                                                userId.toString()
                                        )
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk());

        verify(deviceTokenService)
                .registerDevice(
                        eq(userId),
                        any(DeviceTokenRequest.class)
                );
    }

    @Test
    void removeDevice_usesJwtUserId() throws Exception {

        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/api/v1/users/me/devices/device-token"
                        )
                                .with(
                                        authenticatedAs(
                                                userId.toString()
                                        )
                                )
                )
                .andExpect(status().isNoContent());

        verify(deviceTokenService)
                .removeDevice(
                        userId,
                        "device-token"
                );
    }

    @Test
    void registerDevice_rejectsBlankDeviceToken()
            throws Exception {

        UUID userId = UUID.randomUUID();

        DeviceTokenRequest request =
                new DeviceTokenRequest(
                        "",
                        "ANDROID"
                );

        mockMvc.perform(
                        post("/api/v1/users/me/devices")
                                .with(
                                        authenticatedAs(
                                                userId.toString()
                                        )
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(deviceTokenService);
    }

    @Test
    void registerDevice_rejectsBlankDeviceType()
            throws Exception {

        UUID userId = UUID.randomUUID();

        DeviceTokenRequest request =
                new DeviceTokenRequest(
                        "device-token",
                        ""
                );

        mockMvc.perform(
                        post("/api/v1/users/me/devices")
                                .with(
                                        authenticatedAs(
                                                userId.toString()
                                        )
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(deviceTokenService);
    }
}