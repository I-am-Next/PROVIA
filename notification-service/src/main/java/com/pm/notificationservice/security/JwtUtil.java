package com.pm.notificationservice.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {

    private final byte[] secretBytes;

    public JwtUtil(

            @Value("${jwt.secret}")
            String jwtSecret

    ) {

        this.secretBytes = jwtSecret.getBytes();

    }

    public boolean validateToken(

            String token

    ) {

        try {

            SignedJWT signedJWT = SignedJWT.parse(

                    token

            );

            JWSVerifier verifier = new MACVerifier(

                    secretBytes

            );

            if (!signedJWT.verify(verifier)) {

                return false;

            }

            Date expiration = signedJWT

                    .getJWTClaimsSet()

                    .getExpirationTime();

            return expiration != null
                    && expiration.after(new Date());

        } catch (ParseException | JOSEException ex) {

            return false;

        }

    }

    public UsernamePasswordAuthenticationToken getAuthentication(

            String token

    ) {

        AuthenticatedUser user = getAuthenticatedUser(

                token

        );

        return new UsernamePasswordAuthenticationToken(

                user,

                null,

                List.of(

                        new SimpleGrantedAuthority(

                                "ROLE_" + user.role()

                        )

                )

        );

    }

    public AuthenticatedUser getAuthenticatedUser(

            String token

    ) {

        try {

            SignedJWT jwt = SignedJWT.parse(

                    token

            );

            String type = jwt.getJWTClaimsSet()

                    .getStringClaim("type");

            if (!"access".equals(type)) {

                throw new IllegalArgumentException(

                        "Refresh token cannot authenticate requests."

                );

            }

            UUID userId = UUID.fromString(

                    jwt.getJWTClaimsSet()

                            .getSubject()

            );

            String email = jwt.getJWTClaimsSet()

                    .getStringClaim("email");

            String role = jwt.getJWTClaimsSet()

                    .getStringClaim("role");

            return new AuthenticatedUser(

                    userId,

                    email,

                    role

            );

        } catch (Exception ex) {

            throw new IllegalArgumentException(

                    "Invalid JWT token.",

                    ex

            );

        }

    }

}