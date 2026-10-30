package com.pm.providerservice.client;

import com.pm.providerservice.client.dto.ApiResponse;
import com.pm.providerservice.client.dto.CatalogServiceItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "catalog-service",
        url = "${services.catalog.url}"
)
public interface CatalogClient {

    @GetMapping("/catalog/services/{id}")
    ApiResponse<CatalogServiceItemResponse> getServiceById(
            @PathVariable UUID id
    );
}