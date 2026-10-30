package com.pm.servicecatalog.infrastructure.persistence.adapter;

import com.pm.servicecatalog.domain.model.ServiceCategory;
import com.pm.servicecatalog.domain.ports.out.ServiceCategoryRepository;
import com.pm.servicecatalog.infrastructure.persistence.jpa.JpaServiceCategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ServiceCategoryRepositoryAdapter
        implements ServiceCategoryRepository {

    private final JpaServiceCategoryRepository jpaRepository;

    public ServiceCategoryRepositoryAdapter(
            JpaServiceCategoryRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ServiceCategory save(ServiceCategory category) {
        return jpaRepository.save(category);
    }

    @Override
    public Optional<ServiceCategory> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ServiceCategory> findActiveById(UUID id) {
        return jpaRepository.findByIdAndActiveTrue(id);
    }

    @Override
    public Optional<ServiceCategory> findByName(String name) {
        return jpaRepository.findByName(name);
    }

    @Override
    public List<ServiceCategory> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<ServiceCategory> findAllActive() {
        return jpaRepository.findByActiveTrue();
    }

    @Override
    public List<ServiceCategory> findAllById(List<UUID> ids) {
        return jpaRepository.findAllById(ids);
    }

    @Override
    public List<ServiceCategory> findAllActiveById(
            List<UUID> ids
    ) {
        return jpaRepository.findAllByIdInAndActiveTrue(ids);
    }
}