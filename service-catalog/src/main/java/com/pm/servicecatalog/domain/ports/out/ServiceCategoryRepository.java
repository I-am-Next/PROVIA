package com.pm.servicecatalog.domain.ports.out;

import com.pm.servicecatalog.domain.model.ServiceCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceCategoryRepository {

    ServiceCategory save(ServiceCategory category);

    Optional<ServiceCategory> findById(UUID id);

    Optional<ServiceCategory> findActiveById(UUID id);

    Optional<ServiceCategory> findByName(String name);

    List<ServiceCategory> findAll();

    List<ServiceCategory> findAllActive();

    List<ServiceCategory> findAllById(List<UUID> ids);

    List<ServiceCategory> findAllActiveById(List<UUID> ids);
}