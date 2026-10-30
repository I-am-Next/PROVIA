package com.pm.bookingservice.infrastructure.client;

import com.pm.bookingservice.infrastructure.config.FeignConfig;
import com.pm.bookingservice.integration.dto.ProviderServiceDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "provider-service",
        url = "${provider-service.url}",
        configuration = FeignConfig.class

)
public interface ProviderServiceClient {

    @GetMapping("/providers/services/{id}")
    ProviderServiceDetailsResponse getService(
            @PathVariable UUID id
    );
}
