package com.pm.servicecatalog.domain.ports.in;

import java.util.UUID;

public interface DeleteServiceItemUseCase {

    void deleteService(UUID serviceId);
}