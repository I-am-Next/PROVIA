package com.pm.providerservice.repository;

import com.pm.providerservice.domain.ProviderService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProviderServiceRepository
        extends JpaRepository<ProviderService, UUID> {

    List<ProviderService> findByProviderProfileIdOrderByCreatedAtDesc(
            UUID providerProfileId
    );

    boolean existsByProviderProfileIdAndServiceItemId(
            UUID providerProfileId,
            UUID serviceItemId
    );
}