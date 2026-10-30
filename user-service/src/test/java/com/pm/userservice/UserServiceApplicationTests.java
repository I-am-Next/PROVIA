package com.pm.userservice;

import com.pm.userservice.api.dto.UserProfileResponse;
import com.pm.userservice.api.dto.UserProfileUpdateRequest;
import com.pm.userservice.application.service.impl.UserProfileServiceImpl;
import com.pm.userservice.domain.UserProfile;
import com.pm.userservice.exception.UserProfileNotFoundException;
import com.pm.userservice.infrastructure.persistence.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceApplicationTests {

    @Mock
    private UserProfileRepository repository;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    @Test
    void getMyProfile_returnsProfile() {

        String authUserId = "auth-user-123";

        UserProfile profile =
                new UserProfile(
                        authUserId,
                        "user@example.com"
                );

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.of(profile));

        UserProfileResponse result =
                userProfileService.getMyProfile(authUserId);

        assertNotNull(result);
        assertEquals(authUserId, result.authUserId());
        assertEquals("user@example.com", result.email());

        verify(repository).findByAuthUserId(authUserId);
    }

    @Test
    void getMyProfile_whenProfileDoesNotExist_throwsException() {

        String authUserId = "missing-user";

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserProfileNotFoundException.class,
                () -> userProfileService.getMyProfile(authUserId)
        );

        verify(repository).findByAuthUserId(authUserId);
    }

    @Test
    void updateMyProfile_updatesProfile() {

        String authUserId = "auth-user-123";

        UserProfile profile =
                new UserProfile(
                        authUserId,
                        "user@example.com"
                );

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(
                        "Emmanuel",
                        "Nde",
                        "08012345678"
                );

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.of(profile));

        UserProfileResponse result =
                userProfileService.updateMyProfile(
                        authUserId,
                        request
                );

        assertEquals("Emmanuel", result.firstName());
        assertEquals("Nde", result.lastName());
        assertEquals("08012345678", result.phone());

        verify(repository).findByAuthUserId(authUserId);
    }

    @Test
    void updateMyProfile_whenProfileDoesNotExist_throwsException() {

        String authUserId = "missing-user";

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(
                        "Emmanuel",
                        "Nde",
                        "08012345678"
                );

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserProfileNotFoundException.class,
                () -> userProfileService.updateMyProfile(
                        authUserId,
                        request
                )
        );

        verify(repository).findByAuthUserId(authUserId);
    }
}