package com.pm.notificationservice.realtime;

import com.pm.notificationservice.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationSseControllerTest {

    private SseEmitterRegistry emitterRegistry;
    private NotificationSseController controller;

    private UUID authenticatedUserId;

    @BeforeEach
    void setUp() {

        emitterRegistry = mock(SseEmitterRegistry.class);

        controller = new NotificationSseController(
                emitterRegistry
        );

        authenticatedUserId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser =
                new AuthenticatedUser(
                        authenticatedUserId,
                        "user@example.com",
                        "CUSTOMER"
                );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + authenticatedUser.role()
                                )
                        )
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void stream_registersEmitterForAuthenticatedUser() {

        SseEmitter emitter = new SseEmitter();

        when(emitterRegistry.register(
                authenticatedUserId.toString()
        )).thenReturn(emitter);

        SseEmitter result = controller.stream();

        assertThat(result).isSameAs(emitter);

        verify(emitterRegistry).register(
                authenticatedUserId.toString()
        );
    }

    @Test
    void stream_doesNotUseClientSuppliedUserId() {

        SseEmitter emitter = new SseEmitter();

        when(emitterRegistry.register(
                authenticatedUserId.toString()
        )).thenReturn(emitter);

        controller.stream();

        verify(emitterRegistry)
                .register(authenticatedUserId.toString());

        verify(emitterRegistry, never())
                .register("another-user-id");
    }
}