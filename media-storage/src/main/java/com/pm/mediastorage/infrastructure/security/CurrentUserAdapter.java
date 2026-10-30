package com.pm.mediastorage.infrastructure.security;

import com.pm.mediastorage.domain.port.out.CurrentUserPort;
import com.pm.mediastorage.exception.MediaAccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserAdapter
        implements CurrentUserPort {

    private static final Logger log =
            LoggerFactory.getLogger(
                    CurrentUserAdapter.class
            );

    @Override
    public String getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        log.debug(
                "Resolving current authenticated user. Authentication: {}",
                authentication
        );

        if (authentication == null) {

            log.warn(
                    "No authentication found in SecurityContext"
            );

            throw new MediaAccessDeniedException(
                    "Authentication is required"
            );
        }

        if (!authentication.isAuthenticated()) {

            log.warn(
                    "Authentication exists but is not authenticated"
            );

            throw new MediaAccessDeniedException(
                    "Authentication is required"
            );
        }

        String userId =
                authentication.getName();

        log.debug(
                "Resolved authenticated user ID: {}",
                userId
        );

        if (userId == null
                || userId.isBlank()
                || "anonymousUser".equals(userId)) {

            log.warn(
                    "Invalid authenticated user principal: {}",
                    userId
            );

            throw new MediaAccessDeniedException(
                    "Authentication is required"
            );
        }

        return userId;
    }
}