package com.pm.mediastorage.infrastructure.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class JwtUtil {

    private final byte[] secretBytes;

    public JwtUtil(
            @Value("${jwt.secret}") String jwtSecret
    ) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret must be configured"
            );
        }

        this.secretBytes =
                jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public boolean validateToken(
            String token
    ) {
        try {
            SignedJWT signedJWT =
                    SignedJWT.parse(token);

            JWSVerifier verifier =
                    new MACVerifier(secretBytes);

            if (!signedJWT.verify(verifier)) {
                return false;
            }

            Date expiration =
                    signedJWT
                            .getJWTClaimsSet()
                            .getExpirationTime();

            if (expiration == null) {
                return false;
            }

            return expiration.after(
                    new Date()
            );

        } catch (
                ParseException |
                JOSEException exception
        ) {
            return false;
        }
    }

    public String getUserIdFromToken(
            String token
    ) {
        try {
            String subject =
                    SignedJWT
                            .parse(token)
                            .getJWTClaimsSet()
                            .getSubject();

            if (subject == null || subject.isBlank()) {
                throw new IllegalArgumentException(
                        "JWT subject is missing"
                );
            }

            return subject;

        } catch (ParseException exception) {

            throw new IllegalArgumentException(
                    "Invalid JWT token",
                    exception
            );
        }
    }

    public String getTokenType(
            String token
    ) {
        try {
            String type =
                    SignedJWT
                            .parse(token)
                            .getJWTClaimsSet()
                            .getStringClaim("type");

            if (type == null || type.isBlank()) {
                throw new IllegalArgumentException(
                        "JWT type claim is missing"
                );
            }

            return type;

        } catch (ParseException exception) {

            throw new IllegalArgumentException(
                    "Invalid JWT token",
                    exception
            );
        }
    }

    public String getRoleFromToken(
            String token
    ) {
        try {
            return SignedJWT
                    .parse(token)
                    .getJWTClaimsSet()
                    .getStringClaim("role");

        } catch (ParseException exception) {

            throw new IllegalArgumentException(
                    "Invalid JWT token",
                    exception
            );
        }
    }
}