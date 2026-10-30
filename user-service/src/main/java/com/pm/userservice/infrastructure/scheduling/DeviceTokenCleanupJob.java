package com.pm.userservice.infrastructure.scheduling;

import com.pm.userservice.application.service.DeviceTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeviceTokenCleanupJob {

    private final DeviceTokenService deviceTokenService;

    public DeviceTokenCleanupJob(
            DeviceTokenService deviceTokenService
    ) {
        this.deviceTokenService = deviceTokenService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void runCleanup() {
        deviceTokenService.cleanupOldTokens();
    }
}