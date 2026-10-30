package com.pm.searchservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ProviderSearchCriteria(

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

    public ProviderSearchCriteria {
        page = page == null || page < 0
                ? 0
                : page;

        size = size == null || size <= 0
                ? 20
                : Math.min(size, 100);

        sort = sort == null || sort.isBlank()
                ? "relevance"
                : sort.trim().toLowerCase();

        active = active == null || active;
    }
}