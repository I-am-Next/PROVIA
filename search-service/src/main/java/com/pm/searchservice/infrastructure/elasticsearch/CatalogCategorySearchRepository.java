package com.pm.searchservice.infrastructure.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.UUID;

public interface CatalogCategorySearchRepository
        extends ElasticsearchRepository<
        CatalogCategorySearchDocumentEntity,
        UUID
        > {
}