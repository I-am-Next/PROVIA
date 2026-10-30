package com.pm.authservice.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pm.authservice.config.JwtConfig;
import com.pm.authservice.model.Role;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final long ACCESS_TOKEN_EXPIRATION =
            15 * 60 * 1000L;

    private static final long REFRESH_TOKEN_EXPIRATION =
            7 * 24 * 60 * 60 * 1000L;

    private final byte[] secretBytes;

    public JwtUtil(JwtConfig jwtConfig) {
        this.secretBytes = jwtConfig.getSecret()
                .getBytes(StandardCharsets.UTF_8);
    }

    public String generateAccessToken(
            String userId,
            String email,
            Role role
    ) {
        return generateToken(
                userId,
                email,
                role,
                ACCESS_TOKEN_EXPIRATION,
                "access"
        );
    }

    public String generateRefreshToken(String userId) {
        return generateToken(
                userId,
                null,
                null,
                REFRESH_TOKEN_EXPIRATION,
                "refresh"
        );
    }

    private String generateToken(
            String userId,
            String email,
            Role role,
            long expiration,
            String type
    ) {
        try {
            JWSSigner signer = new MACSigner(secretBytes);

            JWTClaimsSet.Builder claimsBuilder =
                    new JWTClaimsSet.Builder()
                            .subject(userId)
                            .jwtID(UUID.randomUUID().toString())
                            .issueTime(new Date())
                            .expirationTime(
                                    new Date(
                                            System.currentTimeMillis()
                                                    + expiration
                                    )
                            )
                            .claim("type", type);

            if (email != null) {
                claimsBuilder.claim("email", email);
            }

            if (role != null) {
                claimsBuilder.claim("role", role.name());
            }

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsBuilder.build()
            );

            signedJWT.sign(signer);

            return signedJWT.serialize();

        } catch (JOSEException exception) {
            throw new IllegalStateException(
                    "Error generating JWT token.",
                    exception
            );
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier = new MACVerifier(secretBytes);

            if (!signedJWT.verify(verifier)) {
                return false;
            }

            Date expiration =
                    signedJWT.getJWTClaimsSet().getExpirationTime();

            return expiration != null
                    && expiration.after(new Date());

        } catch (ParseException | JOSEException exception) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            return SignedJWT.parse(token)
                    .getJWTClaimsSet()
                    .getSubject();

        } catch (ParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid JWT token.",
                    exception
            );
        }
    }

    public Role getRoleFromToken(String token) {
        try {
            String role = SignedJWT.parse(token)
                    .getJWTClaimsSet()
                    .getStringClaim("role");

            if (role == null) {
                throw new IllegalArgumentException(
                        "Role claim missing in token."
                );
            }

            return Role.valueOf(role);

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid or missing role in JWT token.",
                    exception
            );

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid JWT token.",
                    exception
            );
        }
    }

    public String getTokenType(String token) {
        try {
            return SignedJWT.parse(token)
                    .getJWTClaimsSet()
                    .getStringClaim("type");

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid JWT token.",
                    exception
            );
        }
    }
}