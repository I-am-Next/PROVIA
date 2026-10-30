package com.pm.notificationservice.realtime;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterRegistry {

    private final Map<String, List<SseEmitter>> emitters =
            new ConcurrentHashMap<>();

    public SseEmitter register(
            String userId
    ) {

        SseEmitter emitter =
                new SseEmitter(0L);

        emitters
                .computeIfAbsent(
                        userId,
                        key -> new CopyOnWriteArrayList<>()
                )
                .add(emitter);

        emitter.onCompletion(
                () -> remove(userId, emitter)
        );

        emitter.onTimeout(
                () -> remove(userId, emitter)
        );

        emitter.onError(
                ex -> remove(userId, emitter)
        );

        return emitter;

    }

    public List<SseEmitter> getEmitters(
            String userId
    ) {

        return emitters.getOrDefault(
                userId,
                List.of()
        );

    }

    public void remove(
            String userId,
            SseEmitter emitter
    ) {

        List<SseEmitter> userEmitters =
                emitters.get(userId);

        if (userEmitters == null) {
            return;
        }

        userEmitters.remove(emitter);

        if (userEmitters.isEmpty()) {

            emitters.remove(userId);

        }

    }

}
