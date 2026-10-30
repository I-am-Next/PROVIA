package com.pm.providerservice.security;

import com.pm.providerservice.application.service.ProviderProfileService;
import com.pm.providerservice.application.service.ProviderServiceService;
import com.pm.providerservice.config.SecurityConfig;
import com.pm.providerservice.controller.ProviderProfileController;
import com.pm.providerservice.controller.ProviderServiceController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {
                ProviderProfileController.class,
                ProviderServiceController.class
        }
)
@Import(SecurityConfig.class)
class ProviderSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProviderProfileService providerProfileService;

    @MockBean
    private ProviderServiceService providerServiceService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRequireAuthenticationForProviderProfile() throws Exception {

        mockMvc.perform(
                        get("/providers/profile")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldAllowAuthenticatedProviderProfileRequest() throws Exception {

        mockMvc.perform(
                        get("/providers/profile")
                                .with(jwt())
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void shouldAllowPublicProviderServiceLookupWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/providers/services/{id}",
                                UUID.randomUUID()
                        )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void shouldAllowPublicProviderServicesWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/providers/{providerProfileId}/services",
                                UUID.randomUUID()
                        )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void shouldRequireAuthenticationForCreatingProviderService()
            throws Exception {

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/providers/services")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldRequireAuthenticationForProviderDashboard()
            throws Exception {

        mockMvc.perform(
                        get("/providers/me/services")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }
}