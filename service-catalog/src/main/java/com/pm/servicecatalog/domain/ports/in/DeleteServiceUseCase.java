package com.pm.servicecatalog.domain.ports.in;

import java.util.UUID;

public interface DeleteServiceUseCase {

    void deleteService(UUID serviceId);
}