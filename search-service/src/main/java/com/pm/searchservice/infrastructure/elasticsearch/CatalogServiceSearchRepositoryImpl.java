package com.pm.searchservice.infrastructure.elasticsearch;

import com.pm.searchservice.infrastructure.elasticsearch.CatalogServiceSearchDocumentEntity;
import com.pm.searchservice.infrastructure.elasticsearch.CatalogServiceSearchRepository;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CatalogServiceSearchRepositoryImpl
        implements CatalogServiceSearchRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public CatalogServiceSearchRepositoryImpl(
            ElasticsearchOperations elasticsearchOperations
    ) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Optional<CatalogServiceSearchDocumentEntity> findById(
            UUID serviceItemId
    ) {

        return Optional.ofNullable(
                elasticsearchOperations.get(
                        serviceItemId.toString(),
                        CatalogServiceSearchDocumentEntity.class
                )
        );
    }

    @Override
    public CatalogServiceSearchDocumentEntity save(
            CatalogServiceSearchDocumentEntity document
    ) {

        return elasticsearchOperations.save(document);
    }

    @Override
    public void deleteById(
            UUID serviceItemId
    ) {

        elasticsearchOperations.delete(
                serviceItemId.toString(),
                CatalogServiceSearchDocumentEntity.class
        );
    }

    @Override
    public List<CatalogServiceSearchDocumentEntity> findByCategoryId(
            UUID categoryId
    ) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t
                        .field("categoryId")
                        .value(categoryId.toString())
                ))
                .build();

        SearchHits<CatalogServiceSearchDocumentEntity> hits =
                elasticsearchOperations.search(
                        query,
                        CatalogServiceSearchDocumentEntity.class
                );

        return hits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent())
                .toList();
    }
}
