package com.pm.notificationservice.integration.feign;

import java.util.UUID;

public record UserProfileResponse(

        UUID id,

        String firstName,

        String lastName,

        String email,

        String phoneNumber

) {
}