package com.pm.userservice.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String authUserId;

    @Column(nullable = false)
    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    @Column(nullable = false)
    private Instant createdAt;

    protected UserProfile() {
    }

    public UserProfile(
            String authUserId,
            String email
    ) {
        this.authUserId = authUserId;
        this.email = email;
        this.createdAt = Instant.now();
    }

    public void updateProfile(
            String firstName,
            String lastName,
            String phone
    ) {

        if (firstName != null) {
            this.firstName = firstName;
        }

        if (lastName != null) {
            this.lastName = lastName;
        }

        if (phone != null) {
            this.phone = phone;
        }
    }

    public Long getId() {
        return id;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}