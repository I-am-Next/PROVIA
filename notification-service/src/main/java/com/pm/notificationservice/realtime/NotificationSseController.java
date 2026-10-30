package com.pm.notificationservice.realtime;

import com.pm.notificationservice.security.SecurityUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationSseController {

    private final SseEmitterRegistry emitterRegistry;

    public NotificationSseController(
            SseEmitterRegistry emitterRegistry
    ) {
        this.emitterRegistry = emitterRegistry;
    }

    @GetMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter stream() {

        UUID userId = SecurityUtils.getCurrentUserId();

        return emitterRegistry.register(
                userId.toString()
        );
    }
}