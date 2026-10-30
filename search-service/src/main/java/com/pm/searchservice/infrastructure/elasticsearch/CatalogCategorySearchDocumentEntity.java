package com.pm.searchservice.infrastructure.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.UUID;

@Document(indexName = "catalog-categories")
public class CatalogCategorySearchDocumentEntity {

    @Id
    private UUID categoryId;

    @Field(type = FieldType.Text)
    private String categoryName;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Boolean)
    private boolean active;

    protected CatalogCategorySearchDocumentEntity() {
    }

    public CatalogCategorySearchDocumentEntity(
            UUID categoryId,
            String categoryName,
            String description,
            boolean active
    ) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.active = active;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}