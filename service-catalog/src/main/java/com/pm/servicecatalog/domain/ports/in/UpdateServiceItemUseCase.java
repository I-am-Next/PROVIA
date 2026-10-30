package com.pm.servicecatalog.domain.ports.in;

import com.pm.servicecatalog.application.dto.ServiceItemResponse;
import com.pm.servicecatalog.application.dto.UpdateServiceRequest;

import java.util.UUID;

public interface UpdateServiceItemUseCase {

    ServiceItemResponse updateService(
            UUID serviceId,
            UpdateServiceRequest request
    );
}