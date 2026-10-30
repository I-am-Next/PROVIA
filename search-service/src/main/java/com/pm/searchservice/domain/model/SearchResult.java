package com.pm.searchservice.domain.model;

import java.util.List;

public record SearchResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public SearchResult {
        content = content == null
                ? List.of()
                : List.copyOf(content);

        page = Math.max(page, 0);
        size = Math.max(size, 1);

        totalElements = Math.max(totalElements, 0);

        totalPages = calculateTotalPages(
                totalElements,
                size
        );
    }

    private static int calculateTotalPages(
            long totalElements,
            int size
    ) {
        if (totalElements == 0) {
            return 0;
        }

        return (int) Math.ceil(
                (double) totalElements / size
        );
    }

    public boolean hasNext() {
        return page + 1 < totalPages;
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }
}