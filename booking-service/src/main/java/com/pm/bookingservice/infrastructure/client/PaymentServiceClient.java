package com.pm.bookingservice.infrastructure.client;

import com.pm.bookingservice.integration.dto.EscrowResponse;
import com.pm.bookingservice.integration.dto.ReleaseMaterialsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "payment-service",
        url = "${payment-service.url}",
        path = "/api/internal/escrows"
)
public interface PaymentServiceClient {

    @PostMapping("/{bookingId}/release-materials")
    EscrowResponse releaseMaterials(

            @PathVariable UUID bookingId,

            @RequestBody ReleaseMaterialsRequest request

    );
    @PostMapping("/{bookingId}/release-labour")
    EscrowResponse releaseLabour(

            @PathVariable
            UUID bookingId

    );

}