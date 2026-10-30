package com.pm.providerservice.repository;

import com.pm.providerservice.domain.ProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderProfileRepository
        extends JpaRepository<ProviderProfile, UUID> {

    Optional<ProviderProfile> findByAuthUserId(String authUserId);

    List<ProviderProfile> findByCategory(String category);

    boolean existsByAuthUserId(String authUserId);
}