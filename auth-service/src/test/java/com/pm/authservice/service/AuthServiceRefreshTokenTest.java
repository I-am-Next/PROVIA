package com.pm.authservice.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pm.authservice.config.JwtConfig;
import com.pm.authservice.dto.response.AuthResponse;
import com.pm.authservice.exception.InvalidTokenException;
import com.pm.authservice.model.Role;
import com.pm.authservice.model.User;
import com.pm.authservice.repository.UserRepository;
import com.pm.authservice.util.JwtUtil;
import com.pm.contracts.auth.UserRegisteredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRefreshTokenTest {

    private static final String JWT_SECRET =
            "test-only-secret-key-for-auth-service";

    private static final long REFRESH_TOKEN_EXPIRY =
            7L * 24 * 60 * 60 * 1000;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RedisRefreshTokenService redisRefreshTokenService;

    @Mock
    private KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    private JwtUtil jwtUtil;
    private AuthServiceImpl authService;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        JwtConfig jwtConfig = new JwtConfig(JWT_SECRET);
        jwtUtil = new JwtUtil(jwtConfig);

        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                jwtUtil,
                redisRefreshTokenService,
                kafkaTemplate
        );

        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
        user.setEnabled(true);
    }

    @Test
    void refreshTokenShouldRotateRefreshToken() {
        String oldRefreshToken =
                jwtUtil.generateRefreshToken(userId.toString());

        when(redisRefreshTokenService.getUserId(oldRefreshToken))
                .thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(redisRefreshTokenService.rotate(
                eq(oldRefreshToken),
                anyString(),
                eq(userId),
                eq(REFRESH_TOKEN_EXPIRY)
        )).thenReturn(true);

        AuthResponse response =
                authService.refreshToken(oldRefreshToken);

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());

        assertNotEquals(
                oldRefreshToken,
                response.refreshToken(),
                "The replacement refresh token must be unique"
        );

        assertTrue(jwtUtil.validateToken(response.accessToken()));
        assertTrue(jwtUtil.validateToken(response.refreshToken()));

        assertEquals(
                "access",
                jwtUtil.getTokenType(response.accessToken())
        );

        assertEquals(
                "refresh",
                jwtUtil.getTokenType(response.refreshToken())
        );

        verify(redisRefreshTokenService).rotate(
                eq(oldRefreshToken),
                eq(response.refreshToken()),
                eq(userId),
                eq(REFRESH_TOKEN_EXPIRY)
        );

        verify(redisRefreshTokenService, never())
                .delete(oldRefreshToken);

        verify(redisRefreshTokenService, never())
                .store(
                        anyString(),
                        eq(userId),
                        anyLong()
                );
    }

    @Test
    void refreshTokenShouldRejectRevokedToken() {
        String refreshToken =
                jwtUtil.generateRefreshToken(userId.toString());

        // A revoked token has no active Redis mapping.
        when(redisRefreshTokenService.getUserId(refreshToken))
                .thenReturn(null);

        assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(redisRefreshTokenService, never()).rotate(
                anyString(),
                anyString(),
                eq(userId),
                anyLong()
        );
    }

    @Test
    void refreshTokenShouldRejectExpiredToken() throws Exception {
        String expiredRefreshToken =
                createExpiredRefreshToken(userId);

        assertFalse(jwtUtil.validateToken(expiredRefreshToken));

        assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshToken(expiredRefreshToken)
        );

        verify(redisRefreshTokenService, never())
                .getUserId(anyString());

        verify(redisRefreshTokenService, never()).rotate(
                anyString(),
                anyString(),
                eq(userId),
                anyLong()
        );
    }

    @Test
    void refreshTokenShouldRejectReplayedTokenAfterRotation() {
        String oldRefreshToken =
                jwtUtil.generateRefreshToken(userId.toString());

        when(redisRefreshTokenService.getUserId(oldRefreshToken))
                .thenReturn(userId.toString())
                .thenReturn(null);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(redisRefreshTokenService.rotate(
                eq(oldRefreshToken),
                anyString(),
                eq(userId),
                eq(REFRESH_TOKEN_EXPIRY)
        )).thenReturn(true);

        AuthResponse firstResponse =
                authService.refreshToken(oldRefreshToken);

        assertNotNull(firstResponse.refreshToken());

        assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshToken(oldRefreshToken)
        );

        verify(redisRefreshTokenService, times(1)).rotate(
                eq(oldRefreshToken),
                eq(firstResponse.refreshToken()),
                eq(userId),
                eq(REFRESH_TOKEN_EXPIRY)
        );
    }

    @Test
    void refreshTokenShouldRejectWhenAtomicRotationFails() {
        String refreshToken =
                jwtUtil.generateRefreshToken(userId.toString());

        when(redisRefreshTokenService.getUserId(refreshToken))
                .thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Simulates another request consuming the token first.
        when(redisRefreshTokenService.rotate(
                eq(refreshToken),
                anyString(),
                eq(userId),
                eq(REFRESH_TOKEN_EXPIRY)
        )).thenReturn(false);

        assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(redisRefreshTokenService).rotate(
                eq(refreshToken),
                anyString(),
                eq(userId),
                eq(REFRESH_TOKEN_EXPIRY)
        );
    }

    @Test
    void refreshTokenShouldRejectAccessToken() {
        String accessToken = jwtUtil.generateAccessToken(
                userId.toString(),
                user.getEmail(),
                user.getRole()
        );

        assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshToken(accessToken)
        );

        verify(redisRefreshTokenService, never())
                .getUserId(anyString());

        verify(redisRefreshTokenService, never()).rotate(
                anyString(),
                anyString(),
                eq(userId),
                anyLong()
        );
    }

    @Test
    void logoutShouldRevokeRefreshToken() {
        String refreshToken =
                jwtUtil.generateRefreshToken(userId.toString());

        authService.logout(refreshToken);

        verify(redisRefreshTokenService).delete(refreshToken);
    }

    @Test
    void logoutAllShouldRevokeAllUserTokens() {
        authService.logoutAll(userId);

        verify(redisRefreshTokenService)
                .deleteByUserId(userId);
    }

    private String createExpiredRefreshToken(UUID userId)
            throws Exception {

        JWSSigner signer = new MACSigner(
                JWT_SECRET.getBytes(StandardCharsets.UTF_8)
        );

        long now = System.currentTimeMillis();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date(now - 120_000))
                .expirationTime(new Date(now - 60_000))
                .claim("type", "refresh")
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }
}