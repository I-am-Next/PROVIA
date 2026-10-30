package com.pm.servicecatalog.infrastructure.persistence.jpa;

import com.pm.servicecatalog.domain.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaServiceCategoryRepository
        extends JpaRepository<ServiceCategory, UUID> {

    Optional<ServiceCategory> findByName(String name);

    Optional<ServiceCategory> findByIdAndActiveTrue(
            UUID id
    );

    List<ServiceCategory> findByActiveTrue();

    List<ServiceCategory> findAllByIdInAndActiveTrue(
            List<UUID> ids
    );
}