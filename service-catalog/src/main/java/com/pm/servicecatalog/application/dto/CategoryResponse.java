package com.pm.servicecatalog.application.dto;

import java.util.List;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        boolean active,
        List<ServiceItemResponse> services
) {}