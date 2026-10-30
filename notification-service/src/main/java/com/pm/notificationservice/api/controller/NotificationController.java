package com.pm.notificationservice.api.controller;

import com.pm.notificationservice.api.dto.response.NotificationCountResponse;
import com.pm.notificationservice.api.dto.response.NotificationResponse;
import com.pm.notificationservice.api.error.response.ApiResponse;
import com.pm.notificationservice.application.mapper.NotificationMapper;
import com.pm.notificationservice.application.service.NotificationCommandService;
import com.pm.notificationservice.application.service.NotificationQueryService;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import com.pm.notificationservice.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationQueryService queryService;

    private final NotificationCommandService commandService;

    private final NotificationMapper notificationMapper;

    public NotificationController(

            NotificationQueryService queryService,

            NotificationCommandService commandService,

            NotificationMapper notificationMapper

    ) {

        this.queryService = queryService;
        this.commandService = commandService;
        this.notificationMapper = notificationMapper;

    }

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size

    ) {

        UUID userId = SecurityUtils.getCurrentUserId();

        Pageable pageable = PageRequest.of(

                page,

                size

        );

        Page<NotificationResponse> response =
                queryService
                        .getNotifications(

                                userId,

                                pageable

                        )
                        .map(notificationMapper::toResponse);

        return ApiResponse.ok(

                response

        );

    }

    @GetMapping("/unread")
    public ApiResponse<Page<NotificationResponse>> getUnreadNotifications(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size

    ) {

        UUID userId = SecurityUtils.getCurrentUserId();

        Pageable pageable = PageRequest.of(

                page,

                size

        );

        Page<NotificationResponse> response =
                queryService
                        .getUnreadNotifications(

                                userId,

                                pageable

                        )
                        .map(notificationMapper::toResponse);

        return ApiResponse.ok(

                response

        );

    }

    @GetMapping("/type/{type}")
    public ApiResponse<Page<NotificationResponse>> getNotificationsByType(

            @PathVariable NotificationType type,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size

    ) {

        UUID userId = SecurityUtils.getCurrentUserId();

        Pageable pageable = PageRequest.of(

                page,

                size

        );

        Page<NotificationResponse> response =
                queryService
                        .getNotificationsByType(

                                userId,

                                type,

                                pageable

                        )
                        .map(notificationMapper::toResponse);

        return ApiResponse.ok(

                response

        );

    }

    @GetMapping("/read")
    public ApiResponse<Page<NotificationResponse>> getReadNotifications(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size

    ) {

        UUID userId = SecurityUtils.getCurrentUserId();

        Pageable pageable = PageRequest.of(

                page,

                size

        );

        Page<NotificationResponse> response =
                queryService
                        .getNotificationsByReadStatus(

                                userId,

                                true,

                                pageable

                        )
                        .map(notificationMapper::toResponse);

        return ApiResponse.ok(

                response

        );

    }

    @GetMapping("/count")
    public ApiResponse<NotificationCountResponse> getUnreadCount() {

        UUID userId = SecurityUtils.getCurrentUserId();

        long count = queryService.getUnreadCount(

                userId

        );

        return ApiResponse.ok(

                new NotificationCountResponse(

                        count

                )

        );

    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(

            @PathVariable UUID notificationId

    ) {

        commandService.markAsRead(

                notificationId,

                SecurityUtils.getCurrentUserId()

        );

        return ApiResponse.ok();

    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {

        commandService.markAllAsRead(

                SecurityUtils.getCurrentUserId()

        );

        return ApiResponse.ok();

    }

}