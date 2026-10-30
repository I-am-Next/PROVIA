package com.pm.servicecatalog.domain.ports.in;

import com.pm.servicecatalog.application.dto.CreateServiceRequest;
import com.pm.servicecatalog.application.dto.ServiceItemResponse;

public interface CreateServiceUseCase {

    ServiceItemResponse createService(CreateServiceRequest request);

}