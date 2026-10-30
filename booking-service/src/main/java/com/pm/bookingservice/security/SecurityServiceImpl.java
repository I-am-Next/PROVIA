package com.pm.bookingservice.security;

import com.pm.bookingservice.domain.exception.UserNotAuthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Override
    public UUID getAuthenticatedUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthenticatedException();
        }

        String userIdStr = authentication.getName();

        try {

            return UUID.fromString(userIdStr);

        } catch (IllegalArgumentException ex) {

            throw new UserNotAuthenticatedException();

        }
    }


}
