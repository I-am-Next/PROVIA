package com.pm.notificationservice.api.controller;

import com.pm.notificationservice.application.mapper.NotificationMapper;
import com.pm.notificationservice.application.service.NotificationCommandService;
import com.pm.notificationservice.application.service.NotificationQueryService;
import com.pm.notificationservice.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationControllerPaginationTest {

    private NotificationQueryService queryService;

    private NotificationCommandService commandService;

    private NotificationMapper notificationMapper;

    private NotificationController controller;

    private UUID authenticatedUserId;

    @BeforeEach
    void setUp() {

        queryService =
                mock(NotificationQueryService.class);

        commandService =
                mock(NotificationCommandService.class);

        notificationMapper =
                mock(NotificationMapper.class);

        controller =
                new NotificationController(
                        queryService,
                        commandService,
                        notificationMapper
                );

        authenticatedUserId =
                UUID.randomUUID();

        AuthenticatedUser user =
                new AuthenticatedUser(
                        authenticatedUserId,
                        "user@example.com",
                        "CUSTOMER"
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_CUSTOMER"
                                )
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void currentImplementation_acceptsOversizedPage() {

        /*
         * This test documents the current implementation.
         *
         * The controller currently accepts a page size of 1000.
         *
         * Once MAX_PAGE_SIZE validation is added to the controller,
         * this test should be replaced with a test that expects
         * the oversized page request to be rejected.
         */

        when(
                queryService.getNotifications(
                        eq(authenticatedUserId),
                        any()
                )
        ).thenReturn(
                Page.empty()
        );

        controller.getNotifications(
                0,
                1000
        );

        verify(queryService)
                .getNotifications(
                        eq(authenticatedUserId),
                        argThat(pageable ->
                                pageable.getPageNumber() == 0
                                        && pageable.getPageSize() == 1000
                        )
                );
    }
}