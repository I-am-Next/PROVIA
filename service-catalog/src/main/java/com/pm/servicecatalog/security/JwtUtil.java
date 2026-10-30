package com.pm.servicecatalog.security;

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

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
        this.secretBytes = jwtSecret.getBytes();
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier =
                    new MACVerifier(secretBytes);

            if (!signedJWT.verify(verifier)) {
                return false;
            }

            Date expiration =
                    signedJWT.getJWTClaimsSet()
                            .getExpirationTime();

            return expiration != null
                    && expiration.after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            return SignedJWT.parse(token)
                    .getJWTClaimsSet()
                    .getSubject();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public String getRoleFromToken(String token) {
        try {
            return SignedJWT.parse(token)
                    .getJWTClaimsSet()
                    .getStringClaim("role");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
