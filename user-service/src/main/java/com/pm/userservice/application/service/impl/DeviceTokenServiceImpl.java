package com.pm.userservice.application.service.impl;

import com.pm.userservice.api.dto.DeviceTokenRequest;
import com.pm.userservice.application.service.DeviceTokenService;
import com.pm.userservice.domain.DeviceType;
import com.pm.userservice.domain.UserDeviceToken;
import com.pm.userservice.infrastructure.persistence.UserDeviceTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final UserDeviceTokenRepository repository;

    public DeviceTokenServiceImpl(
            UserDeviceTokenRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public void registerDevice(
            UUID userId,
            DeviceTokenRequest request
    ) {

        DeviceType type =
                DeviceType.valueOf(
                        request.deviceType().toUpperCase()
                );

        repository.findByDeviceToken(request.deviceToken())
                .ifPresentOrElse(
                        UserDeviceToken::updateLastUsed,
                        () -> repository.save(
                                new UserDeviceToken(
                                        userId,
                                        request.deviceToken(),
                                        type
                                )
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserDeviceTokens(UUID userId) {

        return repository.findByUserId(userId)
                .stream()
                .map(UserDeviceToken::getDeviceToken)
                .toList();
    }

    @Override
    public void cleanupOldTokens() {

        Instant cutoff =
                Instant.now().minus(30, ChronoUnit.DAYS);

        repository.deleteByLastUsedAtBefore(cutoff);
    }

    @Override
    public void removeDevice(
            UUID userId,
            String deviceToken
    ) {

        repository.findByDeviceToken(deviceToken)
                .filter(device -> device.getUserId().equals(userId))
                .ifPresent(repository::delete);
    }
}