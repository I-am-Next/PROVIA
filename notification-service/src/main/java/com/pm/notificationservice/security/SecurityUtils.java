package com.pm.notificationservice.security;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new IllegalStateException(
                    "No authenticated user found."
            );

        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AuthenticatedUser authenticatedUser)) {

            throw new IllegalStateException(
                    "Invalid authenticated principal."
            );

        }

        return authenticatedUser;

    }

    public static UUID getCurrentUserId() {

        return getCurrentUser().userId();

    }

    public static String getCurrentEmail() {

        return getCurrentUser().email();

    }

    public static String getCurrentRole() {

        return getCurrentUser().role();

    }

}
