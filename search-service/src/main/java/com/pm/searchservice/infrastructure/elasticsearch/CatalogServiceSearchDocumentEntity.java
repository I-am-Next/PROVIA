package com.pm.searchservice.infrastructure.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.UUID;

@Document(indexName = "catalog-services")
public class CatalogServiceSearchDocumentEntity {

    @Id
    private UUID serviceItemId;

    @Field(type = FieldType.Text)
    private String serviceName;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private UUID categoryId;

    @Field(type = FieldType.Text)
    private String categoryName;

    @Field(type = FieldType.Boolean)
    private boolean active;

    protected CatalogServiceSearchDocumentEntity() {
    }

    public CatalogServiceSearchDocumentEntity(
            UUID serviceItemId,
            String serviceName,
            String description,
            UUID categoryId,
            String categoryName,
            boolean active
    ) {
        this.serviceItemId = serviceItemId;
        this.serviceName = serviceName;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.active = active;
    }

    public UUID getServiceItemId() {
        return serviceItemId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public boolean isActive() {
        return active;
    }
}
