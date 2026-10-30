package com.pm.authservice.service;

import com.pm.authservice.dto.request.LoginRequest;
import com.pm.authservice.dto.request.RegisterUserRequest;
import com.pm.authservice.dto.response.AuthResponse;
import com.pm.authservice.exception.EmailAlreadyExistsException;
import com.pm.authservice.exception.InvalidCredentialsException;
import com.pm.authservice.exception.InvalidTokenException;
import com.pm.authservice.model.Role;
import com.pm.authservice.model.User;
import com.pm.authservice.repository.UserRepository;
import com.pm.authservice.util.JwtUtil;
import com.pm.contracts.auth.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final long REFRESH_TOKEN_EXPIRY =
            7 * 24 * 60 * 60 * 1000L;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisRefreshTokenService redisRefreshTokenService;
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public AuthServiceImpl(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            RedisRefreshTokenService redisRefreshTokenService,
            KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisRefreshTokenService = redisRefreshTokenService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public AuthResponse registerUser(RegisterUserRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn(
                    "Registration failed - email already exists: {}",
                    request.email()
            );

            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        // Convert the permitted registration role to the internal role.
        // RegistrationRole cannot represent ADMIN.
        user.setRole(Role.valueOf(request.role().name()));

        user.setEnabled(true);

        user = userRepository.save(user);

        log.info("User registered successfully: {}", user.getEmail());
        log.info("Publishing UserRegisteredEvent...");

        kafkaTemplate.send(
                "user-registered",
                new UserRegisteredEvent(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getRole().name()
                )
        );

        log.info(
                "UserRegisteredEvent published for userId={}",
                user.getId()
        );

        return generateAndSaveTokens(user);
    }

    @Override
    public AuthResponse loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn(
                            "Login failed - user not found: {}",
                            request.email()
                    );

                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            log.warn(
                    "Login failed - invalid password: {}",
                    request.email()
            );

            throw new InvalidCredentialsException();
        }

        log.info("User logged in successfully: {}", user.getEmail());

        return generateAndSaveTokens(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {

        log.info("Refresh token attempt");

        // 1. Validate JWT signature and expiry.
        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Invalid or expired refresh token");
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        // 2. Accept refresh tokens only.
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            log.warn("Wrong token type used for refresh");
            throw new InvalidTokenException("Invalid token type");
        }

        // 3. Resolve the currently active Redis session.
        String userIdStr = redisRefreshTokenService.getUserId(refreshToken);

        if (userIdStr == null) {
            log.warn("Refresh token not found in Redis");
            throw new InvalidTokenException("Refresh token not found");
        }

        UUID userId = UUID.fromString(userIdStr);

        // 4. Load the user before attempting rotation.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for token");
                    return new InvalidTokenException("User not found");
                });

        // 5. Generate replacement tokens.
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole()
        );

        String newRefreshToken = jwtUtil.generateRefreshToken(
                user.getId().toString()
        );

        // 6. Atomically consume the old token and store the replacement.
        boolean rotated = redisRefreshTokenService.rotate(
                refreshToken,
                newRefreshToken,
                user.getId(),
                REFRESH_TOKEN_EXPIRY
        );

        if (!rotated) {
            log.warn("Refresh token was already consumed or revoked");
            throw new InvalidTokenException("Refresh token not found");
        }

        log.info("Tokens refreshed for user: {}", user.getEmail());

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    private AuthResponse generateAndSaveTokens(User user) {

        String accessToken = jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId().toString()
        );

        redisRefreshTokenService.store(
                refreshToken,
                user.getId(),
                REFRESH_TOKEN_EXPIRY
        );

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public void logout(String refreshToken) {

        log.info("Logout request");

        redisRefreshTokenService.delete(refreshToken);
    }

    @Override
    public void logoutAll(UUID userId) {

        log.info("Logging out all sessions for user: {}", userId);

        redisRefreshTokenService.deleteByUserId(userId);
    }
}