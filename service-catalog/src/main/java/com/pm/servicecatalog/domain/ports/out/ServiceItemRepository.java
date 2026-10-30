package com.pm.servicecatalog.domain.ports.out;

import com.pm.servicecatalog.domain.model.ServiceItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceItemRepository {

    ServiceItem save(ServiceItem serviceItem);

    Optional<ServiceItem> findById(UUID id);

    Optional<ServiceItem> findByIdAndActiveTrue(UUID id);

    List<ServiceItem> findByCategoryId(UUID categoryId);

    List<ServiceItem> findByCategoryIdAndActiveTrue(UUID categoryId);
}