package com.pm.notificationservice.realtime;

import com.pm.notificationservice.domain.model.Notification;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Service
public class NotificationStreamService {

    private final SseEmitterRegistry emitterRegistry;

    public NotificationStreamService(
            SseEmitterRegistry emitterRegistry
    ) {
        this.emitterRegistry = emitterRegistry;
    }

    public void publish(
            String userId,
            Notification notification
    ) {

        List<SseEmitter> emitters =
                emitterRegistry.getEmitters(
                        userId
                );

        for (SseEmitter emitter : emitters) {

            try {

                emitter.send(
                        SseEmitter.event()
                                .name("notification")
                                .data(notification)
                );

            } catch (IOException ex) {

                emitter.complete();

                emitterRegistry.remove(
                        userId,
                        emitter
                );

            }

        }

    }

}