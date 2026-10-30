package com.pm.searchservice.infrastructure.elasticsearch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogServiceSearchRepository {

    Optional<CatalogServiceSearchDocumentEntity> findById(
            UUID serviceItemId
    );

    CatalogServiceSearchDocumentEntity save(
            CatalogServiceSearchDocumentEntity document
    );

    void deleteById(
            UUID serviceItemId
    );

    List<CatalogServiceSearchDocumentEntity> findByCategoryId(
            UUID categoryId
    );
}
