package com.pm.searchservice.infrastructure.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Document(indexName = "providers")
public class ProviderSearchDocumentEntity {

    @Id
    private UUID providerId;

    @Field(type = FieldType.Text)
    private String providerName;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    private String location;

    @Field(type = FieldType.Double)
    private BigDecimal rating;

    @Field(type = FieldType.Integer)
    private int reviewCount;

    @Field(type = FieldType.Boolean)
    private boolean verified;

    @Field(type = FieldType.Keyword)
    private UUID profilePhotoMediaId;

    @Field(type = FieldType.Nested)
    private List<ServiceSearchItem> services;

    protected ProviderSearchDocumentEntity() {
    }

    public ProviderSearchDocumentEntity(
            UUID providerId,
            String providerName,
            String description,
            String location,
            BigDecimal rating,
            int reviewCount,
            boolean verified,
            UUID profilePhotoMediaId,
            List<ServiceSearchItem> services
    ) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.description = description;
        this.location = location;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.verified = verified;
        this.profilePhotoMediaId = profilePhotoMediaId;
        this.services = services;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public boolean isVerified() {
        return verified;
    }

    public UUID getProfilePhotoMediaId() {
        return profilePhotoMediaId;
    }

    public List<ServiceSearchItem> getServices() {
        return services;
    }

    public static class ServiceSearchItem {

        @Field(type = FieldType.Keyword)
        private UUID providerServiceId;

        @Field(type = FieldType.Keyword)
        private UUID serviceItemId;

        @Field(type = FieldType.Text)
        private String serviceName;

        @Field(type = FieldType.Keyword)
        private UUID categoryId;

        @Field(type = FieldType.Text)
        private String categoryName;

        @Field(type = FieldType.Double)
        private BigDecimal price;

        @Field(type = FieldType.Boolean)
        private boolean active;

        protected ServiceSearchItem() {
        }

        public ServiceSearchItem(
                UUID providerServiceId,
                UUID serviceItemId,
                String serviceName,
                UUID categoryId,
                String categoryName,
                BigDecimal price,
                boolean active
        ) {
            this.providerServiceId = providerServiceId;
            this.serviceItemId = serviceItemId;
            this.serviceName = serviceName;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.price = price;
            this.active = active;
        }

        public UUID getProviderServiceId() {
            return providerServiceId;
        }

        public UUID getServiceItemId() {
            return serviceItemId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public UUID getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public boolean isActive() {
            return active;
        }
    }
}
