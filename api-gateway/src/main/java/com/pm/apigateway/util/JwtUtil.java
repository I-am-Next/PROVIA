package com.pm.apigateway.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;


public class JwtUtil {

    public static String getUserId(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getClaimAsString("sub");
        }
        return "anonymous";
    }
}
