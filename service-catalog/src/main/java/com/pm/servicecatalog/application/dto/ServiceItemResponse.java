package com.pm.servicecatalog.application.dto;

import java.util.UUID;

public record ServiceItemResponse(
        UUID id,
        String name,
        String description,
        UUID categoryId,
        String categoryName,
        boolean active) { }
