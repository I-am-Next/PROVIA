package com.pm.providerservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "provider_services",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_service",
                        columnNames = {
                                "provider_profile_id",
                                "service_item_id"
                        }
                )
        }
)
public class ProviderService {

    @Id
    private UUID id;

    @Column(
            name = "provider_profile_id",
            nullable = false
    )
    private UUID providerProfileId;

    @Column(
            name = "service_item_id",
            nullable = false
    )
    private UUID serviceItemId;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected ProviderService() {
        // JPA
    }

    public ProviderService(
            UUID providerProfileId,
            UUID serviceItemId,
            BigDecimal price
    ) {

        this.id = UUID.randomUUID();
        this.providerProfileId = providerProfileId;
        this.serviceItemId = serviceItemId;
        this.price = price;
        this.active = true;
        this.createdAt = Instant.now();
    }

    // =========================================================
    // DOMAIN BEHAVIOUR
    // =========================================================

    public void updatePrice(
            BigDecimal price
    ) {

        this.price = price;
    }

    public void toggle() {

        this.active = !this.active;
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public UUID getId() {
        return id;
    }

    public UUID getProviderProfileId() {
        return providerProfileId;
    }

    public UUID getServiceItemId() {
        return serviceItemId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}