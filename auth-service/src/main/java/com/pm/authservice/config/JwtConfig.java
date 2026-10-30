package com.pm.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class JwtConfig {

    public static final long ACCESS_TOKEN_EXPIRATION =
            15 * 60 * 1000L;

    private static final int MINIMUM_SECRET_BYTES = 32;

    private final String secret;

    public JwtConfig(@Value("${jwt.secret}") String secret) {
        validateSecret(secret);
        this.secret = secret;
    }

    private void validateSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET must not be empty."
            );
        }

        String normalizedSecret = secret.trim();
        String normalizedLowerCase =
                normalizedSecret.toLowerCase(Locale.ROOT);

        if (normalizedLowerCase.startsWith("replace_with_")
                || normalizedLowerCase.equals("changeme")
                || normalizedLowerCase.equals("change_me")
                || normalizedLowerCase.equals("secret")
                || normalizedLowerCase.equals("dummy")) {

            throw new IllegalStateException(
                    "JWT_SECRET contains a placeholder or insecure "
                            + "default. Configure a strong, randomly "
                            + "generated secret."
            );
        }

        int secretLength = normalizedSecret
                .getBytes(StandardCharsets.UTF_8)
                .length;

        if (secretLength < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET must contain at least "
                            + MINIMUM_SECRET_BYTES
                            + " UTF-8 bytes for HS256."
            );
        }
    }

    public String getSecret() {
        return secret;
    }
}