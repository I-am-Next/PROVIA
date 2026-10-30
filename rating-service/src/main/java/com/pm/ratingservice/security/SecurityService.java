package com.pm.ratingservice.security;

import com.pm.ratingservice.domain.enums.ReviewerRole;

import java.util.UUID;

public interface SecurityService {

    UUID getAuthenticatedUserId();

    ReviewerRole getAuthenticatedUserRole();
}