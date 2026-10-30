package com.pm.ratingservice.security;

import com.pm.ratingservice.domain.enums.ReviewerRole;
import com.pm.ratingservice.domain.exception.UserNotAuthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Override
    public java.util.UUID getAuthenticatedUserId() {

        Authentication authentication =
                getAuthentication();

        String userIdStr = authentication.getName();

        try {

            return java.util.UUID.fromString(userIdStr);

        } catch (IllegalArgumentException ex) {

            throw new UserNotAuthenticatedException();
        }
    }

    @Override
    public ReviewerRole getAuthenticatedUserRole() {

        Authentication authentication =
                getAuthentication();

        return authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority ->
                        authority.equals("ROLE_CUSTOMER")
                                || authority.equals("ROLE_PROVIDER")
                )
                .map(authority ->
                        ReviewerRole.valueOf(
                                authority.substring("ROLE_".length())
                        )
                )
                .findFirst()
                .orElseThrow(
                        UserNotAuthenticatedException::new
                );
    }

    private Authentication getAuthentication() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new UserNotAuthenticatedException();
        }

        return authentication;
    }
}