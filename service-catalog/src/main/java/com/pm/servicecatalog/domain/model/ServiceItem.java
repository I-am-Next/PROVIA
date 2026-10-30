package com.pm.servicecatalog.domain.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "service_items")
public class ServiceItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private UUID categoryId;

    protected ServiceItem() {
        // JPA
    }

    private ServiceItem(
            String name,
            String description,
            UUID categoryId
    ) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.active = true;
    }

    public static ServiceItem create(
            String name,
            String description,
            UUID categoryId
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Service name cannot be empty"
            );
        }

        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "Category ID is required"
            );
        }

        return new ServiceItem(
                name,
                description,
                categoryId
        );
    }

    public void update(
            String name,
            String description,
            UUID categoryId
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Service name cannot be empty"
            );
        }

        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "Category ID is required"
            );
        }

        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public boolean isActive() {
        return active;
    }
}