package com.pm.userservice.config;

import com.pm.userservice.api.dto.UserProfileResponse;
import com.pm.userservice.application.service.DeviceTokenService;
import com.pm.userservice.application.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private DeviceTokenService deviceTokenService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void protectedEndpoint_withoutAuthentication_returnsUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users/me")
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    void authenticatedRequest_canAccessProtectedEndpoint()
            throws Exception {

        String authUserId = "auth-user-123";

        UserProfileResponse response =
                new UserProfileResponse(
                        1L,
                        authUserId,
                        "user@example.com",
                        "Emmanuel",
                        "Nde",
                        "08012345678",
                        Instant.now()
                );

        when(userProfileService.getMyProfile(authUserId))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/v1/users/me")
                        .with(
                                jwt().jwt(jwt ->
                                        jwt.subject(authUserId)
                                )
                        )
        ).andExpect(
                status().isOk()
        );
    }
}