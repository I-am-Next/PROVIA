package com.pm.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@WebFluxTest
@Import({
        SecurityConfig.class,
        JwtDecoderConfig.class,
        GatewaySecurityTest.SecurityTestController.class,
        GatewaySecurityTest.TestJacksonConfiguration.class
})
@TestPropertySource(properties = {
        "jwt.secret=provia-gateway-test-secret-key-123456789"
})
class GatewaySecurityTest {

    private static final String ADMIN_ENDPOINT =
            "/catalog/admin/security-test";

    private static final String TEST_SECRET =
            "provia-gateway-test-secret-key-123456789";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void protectedAdminEndpointShouldRejectMissingToken() {
        webTestClient.get()
                .uri(ADMIN_ENDPOINT)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void protectedAdminEndpointShouldRejectInvalidToken() {
        webTestClient.get()
                .uri(ADMIN_ENDPOINT)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer this-is-not-a-valid-jwt"
                )
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void ordinaryUserShouldBeForbiddenFromAdminEndpoint()
            throws JOSEException {

        String token = generateToken("USER");

        webTestClient.get()
                .uri(ADMIN_ENDPOINT)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void administratorShouldAccessAdminEndpoint()
            throws JOSEException {

        String token = generateToken("ADMIN");

        webTestClient.get()
                .uri(ADMIN_ENDPOINT)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("admin-access-granted");
    }

    @Test
    void refreshTokenShouldBeRejectedFromProtectedEndpoint()
            throws JOSEException {

        String refreshToken = generateToken("ADMIN", "refresh");

        webTestClient.get()
                .uri(ADMIN_ENDPOINT)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + refreshToken
                )
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    private String generateToken(String role) throws JOSEException {
        return generateToken(role, "access");
    }

    private String generateToken(String role, String type)
            throws JOSEException {

        Date now = new Date();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(UUID.randomUUID().toString())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(now)
                .expirationTime(
                        new Date(now.getTime() + 5 * 60 * 1000L)
                )
                .claim("type", type)
                .claim("role", role)
                .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );

        jwt.sign(new MACSigner(
                TEST_SECRET.getBytes(StandardCharsets.UTF_8)
        ));

        return jwt.serialize();
    }

    @RestController
    public static class SecurityTestController {

        @GetMapping("/catalog/admin/security-test")
        public String adminEndpoint() {
            return "admin-access-granted";
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestJacksonConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }
}