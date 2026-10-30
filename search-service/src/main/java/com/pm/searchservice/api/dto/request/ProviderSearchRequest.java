package com.pm.searchservice.api.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record ProviderSearchRequest(
        String query,
        UUID categoryId,
        UUID serviceItemId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal minRating,
        Boolean verified,
        Boolean active,
        Integer page,
        Integer size,
        String sort
) {
}