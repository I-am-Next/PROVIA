package com.pm.servicecatalog.infrastructure.persistence.adapter;

import com.pm.servicecatalog.domain.model.ServiceItem;
import com.pm.servicecatalog.domain.ports.out.ServiceItemRepository;
import com.pm.servicecatalog.infrastructure.persistence.jpa.JpaServiceItemRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ServiceItemRepositoryAdapter
        implements ServiceItemRepository {

    private final JpaServiceItemRepository jpaRepository;

    public ServiceItemRepositoryAdapter(
            JpaServiceItemRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ServiceItem save(ServiceItem serviceItem) {
        return jpaRepository.save(serviceItem);
    }

    @Override
    public Optional<ServiceItem> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ServiceItem> findByIdAndActiveTrue(UUID id) {
        return jpaRepository.findByIdAndActiveTrue(id);
    }

    @Override
    public List<ServiceItem> findByCategoryId(UUID categoryId) {
        return jpaRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<ServiceItem> findByCategoryIdAndActiveTrue(
            UUID categoryId
    ) {
        return jpaRepository.findByCategoryIdAndActiveTrue(categoryId);
    }
}