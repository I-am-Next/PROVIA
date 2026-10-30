package com.pm.providerservice.controller;

import com.pm.providerservice.api.dto.request.CreateProviderServiceRequest;
import com.pm.providerservice.api.dto.request.UpdateProviderServiceRequest;
import com.pm.providerservice.api.dto.response.ProviderServiceDetailsResponse;
import com.pm.providerservice.api.dto.response.ProviderServiceResponse;
import com.pm.providerservice.application.service.ProviderServiceService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/providers")
public class ProviderServiceController {

    private final ProviderServiceService service;

    public ProviderServiceController(
            ProviderServiceService service
    ) {
        this.service = service;
    }

    // =========================================================
    // CREATE SERVICE
    // =========================================================

    @PostMapping("/services")
    public ProviderServiceResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateProviderServiceRequest request
    ) {
        return service.createService(
                jwt,
                request
        );
    }

    // =========================================================
    // PROVIDER DASHBOARD
    // =========================================================

    @GetMapping("/me/services")
    public List<ProviderServiceResponse> getMyServices(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return service.getMyServices(jwt);
    }

    // =========================================================
    // PUBLIC PROVIDER SERVICES
    // =========================================================

    @GetMapping("/{providerProfileId}/services")
    public List<ProviderServiceResponse> getProviderServices(
            @PathVariable UUID providerProfileId
    ) {
        return service.getProviderServices(
                providerProfileId
        );
    }

    // =========================================================
    // UPDATE SERVICE
    // =========================================================

    @PutMapping("/me/services/{id}")
    public ProviderServiceResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProviderServiceRequest request
    ) {
        return service.updateService(
                jwt,
                id,
                request
        );
    }

    // =========================================================
    // DELETE SERVICE
    // =========================================================

    @DeleteMapping("/me/services/{id}")
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        service.deleteService(
                jwt,
                id
        );
    }

    // =========================================================
    // TOGGLE SERVICE
    // =========================================================

    @PatchMapping("/me/services/{id}/toggle")
    public ProviderServiceResponse toggle(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        return service.toggleActive(
                jwt,
                id
        );
    }

    // =========================================================
    // GET SINGLE SERVICE
    // =========================================================

    @GetMapping("/services/{id}")
    public ProviderServiceDetailsResponse getService(
            @PathVariable UUID id
    ) {
        return service.getService(id);
    }
}