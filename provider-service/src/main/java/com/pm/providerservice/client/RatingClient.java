package com.pm.providerservice.client;

import com.pm.providerservice.client.dto.ProviderRatingSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "rating-service",
        url = "${services.rating.url}"
)
public interface RatingClient {

    @GetMapping("/api/v1/ratings/provider/{providerId}/summary")
    ProviderRatingSummaryResponse getProviderRatingSummary(
            @PathVariable("providerId") UUID providerId
    );
}