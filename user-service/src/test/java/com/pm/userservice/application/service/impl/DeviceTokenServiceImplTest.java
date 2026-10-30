package com.pm.userservice.application.service.impl;

import com.pm.userservice.api.dto.DeviceTokenRequest;
import com.pm.userservice.domain.DeviceType;
import com.pm.userservice.domain.UserDeviceToken;
import com.pm.userservice.infrastructure.persistence.UserDeviceTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceImplTest {

    @Mock
    private UserDeviceTokenRepository repository;

    @InjectMocks
    private DeviceTokenServiceImpl deviceTokenService;

    @Test
    void registerDevice_whenTokenDoesNotExist_savesNewToken() {

        UUID userId = UUID.randomUUID();

        DeviceTokenRequest request =
                new DeviceTokenRequest(
                        "device-token-123",
                        "ANDROID"
                );

        when(repository.findByDeviceToken(request.deviceToken()))
                .thenReturn(Optional.empty());

        deviceTokenService.registerDevice(
                userId,
                request
        );

        ArgumentCaptor<UserDeviceToken> captor =
                ArgumentCaptor.forClass(UserDeviceToken.class);

        verify(repository).save(captor.capture());

        UserDeviceToken saved = captor.getValue();

        assertEquals(
                userId,
                saved.getUserId()
        );

        assertEquals(
                "device-token-123",
                saved.getDeviceToken()
        );

        assertEquals(
                DeviceType.ANDROID,
                saved.getDeviceType()
        );
    }

    @Test
    void registerDevice_acceptsLowercaseDeviceType() {

        UUID userId = UUID.randomUUID();

        DeviceTokenRequest request =
                new DeviceTokenRequest(
                        "device-token-123",
                        "android"
                );

        when(repository.findByDeviceToken(request.deviceToken()))
                .thenReturn(Optional.empty());

        deviceTokenService.registerDevice(
                userId,
                request
        );

        verify(repository).save(any(UserDeviceToken.class));
    }

    @Test
    void registerDevice_whenTokenAlreadyExists_updatesLastUsed() {

        UUID userId = UUID.randomUUID();

        UserDeviceToken existing =
                new UserDeviceToken(
                        userId,
                        "existing-token",
                        DeviceType.ANDROID
                );

        DeviceTokenRequest request =
                new DeviceTokenRequest(
                        "existing-token",
                        "ANDROID"
                );

        when(repository.findByDeviceToken("existing-token"))
                .thenReturn(Optional.of(existing));

        deviceTokenService.registerDevice(
                userId,
                request
        );

        verify(repository, never())
                .save(any(UserDeviceToken.class));
    }

    @Test
    void registerDevice_whenDeviceTypeIsInvalid_throwsException() {

        UUID userId = UUID.randomUUID();

        DeviceTokenRequest request =
                new DeviceTokenRequest(
                        "device-token",
                        "BLACKBERRY"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> deviceTokenService.registerDevice(
                        userId,
                        request
                )
        );

        verify(repository, never())
                .save(any(UserDeviceToken.class));
    }

    @Test
    void getUserDeviceTokens_returnsUserTokens() {

        UUID userId = UUID.randomUUID();

        UserDeviceToken first =
                new UserDeviceToken(
                        userId,
                        "token-one",
                        DeviceType.ANDROID
                );

        UserDeviceToken second =
                new UserDeviceToken(
                        userId,
                        "token-two",
                        DeviceType.IOS
                );

        when(repository.findByUserId(userId))
                .thenReturn(List.of(first, second));

        List<String> result =
                deviceTokenService.getUserDeviceTokens(userId);

        assertEquals(
                List.of("token-one", "token-two"),
                result
        );

        verify(repository).findByUserId(userId);
    }

    @Test
    void removeDevice_whenTokenBelongsToUser_deletesToken() {

        UUID userId = UUID.randomUUID();

        UserDeviceToken device =
                new UserDeviceToken(
                        userId,
                        "device-token",
                        DeviceType.ANDROID
                );

        when(repository.findByDeviceToken("device-token"))
                .thenReturn(Optional.of(device));

        deviceTokenService.removeDevice(
                userId,
                "device-token"
        );

        verify(repository).delete(device);
    }

    @Test
    void removeDevice_whenTokenBelongsToDifferentUser_doesNotDelete() {

        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        UserDeviceToken device =
                new UserDeviceToken(
                        ownerId,
                        "device-token",
                        DeviceType.ANDROID
                );

        when(repository.findByDeviceToken("device-token"))
                .thenReturn(Optional.of(device));

        deviceTokenService.removeDevice(
                otherUserId,
                "device-token"
        );

        verify(repository, never())
                .delete(any(UserDeviceToken.class));
    }

    @Test
    void removeDevice_whenTokenDoesNotExist_doesNothing() {

        UUID userId = UUID.randomUUID();

        when(repository.findByDeviceToken("missing-token"))
                .thenReturn(Optional.empty());

        deviceTokenService.removeDevice(
                userId,
                "missing-token"
        );

        verify(repository, never())
                .delete(any(UserDeviceToken.class));
    }

    @Test
    void registerDevice_acceptsSupportedDeviceType() {

        UUID userId = UUID.randomUUID();

        DeviceTokenRequest request =
                new DeviceTokenRequest(
                        "web-device-token",
                        "web"
                );

        when(repository.findByDeviceToken(request.deviceToken()))
                .thenReturn(Optional.empty());

        deviceTokenService.registerDevice(
                userId,
                request
        );

        ArgumentCaptor<UserDeviceToken> captor =
                ArgumentCaptor.forClass(UserDeviceToken.class);

        verify(repository).save(captor.capture());

        UserDeviceToken saved =
                captor.getValue();

        assertEquals(
                userId,
                saved.getUserId()
        );

        assertEquals(
                "web-device-token",
                saved.getDeviceToken()
        );

        assertEquals(
                DeviceType.WEB,
                saved.getDeviceType()
        );
    }

    @Test
    void cleanupOldTokens_deletesTokensBeforeCutoff() {

        deviceTokenService.cleanupOldTokens();

        verify(repository).deleteByLastUsedAtBefore(
                any(Instant.class)
        );
    }
}