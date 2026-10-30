package com.pm.userservice.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

    @Test
    void constructor_setsRequiredFields() {

        UserProfile profile =
                new UserProfile(
                        "auth-user-123",
                        "user@example.com"
                );

        assertEquals(
                "auth-user-123",
                profile.getAuthUserId()
        );

        assertEquals(
                "user@example.com",
                profile.getEmail()
        );

        assertNotNull(profile.getCreatedAt());
    }

    @Test
    void updateProfile_updatesProvidedFields() {

        UserProfile profile =
                new UserProfile(
                        "auth-user-123",
                        "user@example.com"
                );

        profile.updateProfile(
                "Emmanuel",
                "Nde",
                "08012345678"
        );

        assertEquals(
                "Emmanuel",
                profile.getFirstName()
        );

        assertEquals(
                "Nde",
                profile.getLastName()
        );

        assertEquals(
                "08012345678",
                profile.getPhone()
        );
    }

    @Test
    void updateProfile_doesNotOverwriteFieldsWithNull() {

        UserProfile profile =
                new UserProfile(
                        "auth-user-123",
                        "user@example.com"
                );

        profile.updateProfile(
                "Emmanuel",
                "Nde",
                "08012345678"
        );

        profile.updateProfile(
                null,
                null,
                null
        );

        assertEquals(
                "Emmanuel",
                profile.getFirstName()
        );

        assertEquals(
                "Nde",
                profile.getLastName()
        );

        assertEquals(
                "08012345678",
                profile.getPhone()
        );
    }
}