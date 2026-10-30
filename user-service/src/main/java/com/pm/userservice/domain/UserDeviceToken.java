package com.pm.userservice.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_device_tokens")
public class UserDeviceToken {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastUsedAt;

    protected UserDeviceToken() {}

    public UserDeviceToken(UUID userId, String deviceToken, DeviceType deviceType) {
        this.userId = userId;
        this.deviceToken = deviceToken;
        this.deviceType = deviceType;
        this.createdAt = Instant.now();
        this.lastUsedAt = Instant.now();
    }

    // 🔁 Update last usage
    public void updateLastUsed() {
        this.lastUsedAt = Instant.now();
    }

    public UUID getUserId() { return userId; }
    public String getDeviceToken() { return deviceToken; }
    public DeviceType getDeviceType() { return deviceType; }
}
