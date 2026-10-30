package com.pm.servicecatalog.application.service;

import com.pm.contracts.catalog.ServiceItemDeletedEvent;
import com.pm.servicecatalog.application.event.DomainEventPublisher;
import com.pm.servicecatalog.domain.model.ServiceItem;
import com.pm.servicecatalog.domain.ports.in.DeleteServiceItemUseCase;
import com.pm.servicecatalog.domain.ports.out.ServiceItemRepository;
import com.pm.servicecatalog.shared.exception.ServiceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class DeleteServiceItemService
        implements DeleteServiceItemUseCase {

    private final ServiceItemRepository serviceRepository;
    private final DomainEventPublisher eventPublisher;

    public DeleteServiceItemService(
            ServiceItemRepository serviceRepository,
            DomainEventPublisher eventPublisher
    ) {
        this.serviceRepository = serviceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void deleteService(
            UUID serviceId
    ) {

        ServiceItem serviceItem =
                serviceRepository.findById(serviceId)
                        .orElseThrow(
                                () -> new ServiceNotFoundException(serviceId)
                        );

        serviceItem.deactivate();

        serviceRepository.save(serviceItem);

        ServiceItemDeletedEvent event =
                new ServiceItemDeletedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        serviceItem.getId(),
                        serviceItem.getCategoryId()
                );

        eventPublisher.publish(event);
    }
}