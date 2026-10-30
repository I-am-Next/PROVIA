package com.pm.authservice.controller;

import com.pm.authservice.dto.request.RegisterUserRequest;
import com.pm.authservice.dto.request.RegistrationRole;
import com.pm.authservice.dto.response.AuthResponse;
import com.pm.authservice.facade.AuthFacade;
import com.pm.authservice.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthRegistrationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthFacade authFacade;

    /*
     * JwtUtil is required by the JWT filter if that component
     * is discovered in the test application context.
     */
    @MockitoBean
    private JwtUtil jwtUtil;

    @ParameterizedTest
    @EnumSource(RegistrationRole.class)
    void register_shouldAcceptPermittedRoles(
            RegistrationRole role
    ) throws Exception {

        when(authFacade.register(any(RegisterUserRequest.class)))
                .thenReturn(
                        new AuthResponse(
                                "test-access-token",
                                "test-refresh-token"
                        )
                );

        String requestBody = """
                {
                    "fullName": "Jane Doe",
                    "email": "jane@example.com",
                    "password": "test-password-123",
                    "role": "%s"
                }
                """.formatted(role.name());

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        verify(authFacade).register(
                org.mockito.ArgumentMatchers.argThat(
                        request -> request.role() == role
                )
        );
    }

    @Test
    void register_shouldRejectAdminRole() throws Exception {

        String requestBody = """
            {
                "fullName": "Unauthorized Admin",
                "email": "attacker@example.com",
                "password": "test-password-123",
                "role": "ADMIN"
            }
            """;

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authFacade, never())
                .register(any(RegisterUserRequest.class));
    }

    @Test
    void register_shouldRejectMissingRole() throws Exception {

        String requestBody = """
                {
                    "fullName": "Jane Doe",
                    "email": "jane@example.com",
                    "password": "test-password-123"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verify(authFacade, never())
                .register(any(RegisterUserRequest.class));
    }
}