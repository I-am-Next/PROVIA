package com.pm.servicecatalog.infrastructure.persistence.jpa;

import com.pm.servicecatalog.domain.model.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaServiceItemRepository
        extends JpaRepository<ServiceItem, UUID> {

    Optional<ServiceItem> findByIdAndActiveTrue(UUID id);

    List<ServiceItem> findByCategoryId(UUID categoryId);

    List<ServiceItem> findByCategoryIdAndActiveTrue(UUID categoryId);
}