package com.pm.searchservice.infrastructure.config;

import com.pm.searchservice.infrastructure.elasticsearch.CatalogCategorySearchDocumentEntity;
import com.pm.searchservice.infrastructure.elasticsearch.CatalogServiceSearchDocumentEntity;
import com.pm.searchservice.infrastructure.elasticsearch.ProviderSearchDocumentEntity;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public ApplicationRunner initializeElasticsearchIndexes(
            ElasticsearchOperations elasticsearchOperations
    ) {
        return args -> {
            createIndexIfMissing(
                    elasticsearchOperations,
                    ProviderSearchDocumentEntity.class
            );

            createIndexIfMissing(
                    elasticsearchOperations,
                    CatalogServiceSearchDocumentEntity.class
            );

            createIndexIfMissing(
                    elasticsearchOperations,
                    CatalogCategorySearchDocumentEntity.class
            );
        };
    }

    private void createIndexIfMissing(
            ElasticsearchOperations elasticsearchOperations,
            Class<?> documentClass
    ) {
        IndexOperations indexOperations =
                elasticsearchOperations.indexOps(documentClass);

        if (!indexOperations.exists()) {
            indexOperations.create();

            indexOperations.putMapping(
                    indexOperations.createMapping()
            );
        }
    }
}
