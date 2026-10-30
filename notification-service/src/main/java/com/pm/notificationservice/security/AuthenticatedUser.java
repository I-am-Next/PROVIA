package com.pm.notificationservice.security;

import java.util.UUID;

public record AuthenticatedUser(

        UUID userId,

        String email,

        String role

) {
}