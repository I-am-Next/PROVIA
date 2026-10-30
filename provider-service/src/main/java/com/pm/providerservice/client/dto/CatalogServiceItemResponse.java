package com.pm.providerservice.client.dto;

import java.util.UUID;

public record CatalogServiceItemResponse(
        UUID id,
        String name,
        String description,
        UUID categoryId,
        String categoryName,
        boolean active
) {
}
