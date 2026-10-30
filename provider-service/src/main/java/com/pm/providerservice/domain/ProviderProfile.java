package com.pm.providerservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "provider_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_profiles_auth_user_id",
                        columnNames = "auth_user_id"
                )
        }
)
public class ProviderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "auth_user_id",
            nullable = false
    )
    private String authUserId;

    @Column(
            name = "business_name",
            nullable = false
    )
    private String businessName;

    @Column(
            nullable = false,
            unique = true
    )
    private String email;

    @Column(
            nullable = false
    )
    private String category;

    private String description;

    private String phone;

    @Column(
            nullable = false
    )
    private boolean verified = false;

    @Column(
            nullable = false
    )
    private double averageRating = 0.0;

    @Column(
            nullable = false
    )
    private int ratingCount = 0;

    @Column(
            name = "profile_photo_media_id"
    )
    private UUID profilePhotoMediaId;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected ProviderProfile() {
        // JPA
    }

    public ProviderProfile(
            String authUserId,
            String businessName,
            String category,
            String email
    ) {
        this.authUserId = authUserId;
        this.businessName = businessName;
        this.category = category;
        this.email = email;
        this.createdAt = Instant.now();
    }

    public void updateBusinessName(
            String businessName
    ) {
        this.businessName = businessName;
    }

    public void updateCategory(
            String category
    ) {
        this.category = category;
    }

    public void updateDescription(
            String description
    ) {
        this.description = description;
    }

    public void updatePhone(
            String phone
    ) {
        this.phone = phone;
    }

    public void updateProfilePhoto(
            UUID mediaId
    ) {
        this.profilePhotoMediaId = mediaId;
    }

    public void verify() {
        this.verified = true;
    }

    public UUID getId() {
        return id;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public String getEmail() {
        return email;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isVerified() {
        return verified;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public UUID getProfilePhotoMediaId() {
        return profilePhotoMediaId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}