package com.pm.notificationservice.domain.model;

import com.pm.contracts.common.enums.RecipientRole;
import com.pm.notificationservice.domain.model.enums.NotificationPriority;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecipientRole recipientRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationPriority priority;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    private UUID bookingId;

    private UUID paymentId;

    private UUID escrowId;

    @Column(nullable = false)
    private String actionUrl;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private boolean read;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant readAt;

    protected Notification() {
    }

    private Notification(
            UUID id,
            UUID recipientUserId,
            RecipientRole recipientRole,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            UUID bookingId,
            UUID paymentId,
            UUID escrowId,
            String actionUrl,
            String metadata,
            boolean read,
            Instant createdAt,
            Instant readAt
    ) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.recipientRole = recipientRole;
        this.notificationType = notificationType;
        this.priority = priority;
        this.title = title;
        this.message = message;
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.escrowId = escrowId;
        this.actionUrl = actionUrl;
        this.metadata = metadata;
        this.read = read;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public static Notification create(
            UUID recipientUserId,
            RecipientRole recipientRole,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            UUID bookingId,
            UUID paymentId,
            UUID escrowId,
            String actionUrl,
            String metadata
    ) {
        return new Notification(
                UUID.randomUUID(),
                recipientUserId,
                recipientRole,
                notificationType,
                priority,
                title,
                message,
                bookingId,
                paymentId,
                escrowId,
                actionUrl,
                metadata,
                false,
                Instant.now(),
                null
        );
    }

    public void markAsRead() {
        if (read) {
            return;
        }

        this.read = true;
        this.readAt = Instant.now();
    }

    public boolean isRead() {
        return read;
    }

    public boolean isUnread() {
        return !read;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public RecipientRole getRecipientRole() {
        return recipientRole;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getEscrowId() {
        return escrowId;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public String getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
