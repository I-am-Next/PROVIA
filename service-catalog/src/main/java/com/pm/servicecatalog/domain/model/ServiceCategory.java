package com.pm.servicecatalog.domain.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "service_categories")
public class ServiceCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean active = true;

    protected ServiceCategory() {
        // JPA
    }

    private ServiceCategory(
            String name,
            String description
    ) {
        this.name = name;
        this.description = description;
        this.active = true;
    }

    public static ServiceCategory create(
            String name,
            String description
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Category name cannot be empty"
            );
        }

        return new ServiceCategory(
                name,
                description
        );
    }

    public void update(
            String name,
            String description
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Category name cannot be empty"
            );
        }

        this.name = name;
        this.description = description;
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

    public boolean isActive() {
        return active;
    }
}