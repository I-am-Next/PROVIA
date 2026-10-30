package com.pm.providerservice.controller;

import com.pm.providerservice.api.dto.OwnerProviderProfileResponse;
import com.pm.providerservice.api.dto.ProviderProfileCreateRequest;
import com.pm.providerservice.api.dto.ProviderProfileUpdateRequest;
import com.pm.providerservice.api.dto.PublicProviderProfileResponse;
import com.pm.providerservice.application.service.ProviderProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/providers/profile")
public class ProviderProfileController {

    private final ProviderProfileService providerProfileService;

    public ProviderProfileController(
            ProviderProfileService providerProfileService
    ) {
        this.providerProfileService = providerProfileService;
    }

    @PostMapping
    public ResponseEntity<OwnerProviderProfileResponse> createMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProviderProfileCreateRequest request
    ) {
        return ResponseEntity.ok(
                providerProfileService.createMyProfile(
                        jwt,
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<OwnerProviderProfileResponse> getMyProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                providerProfileService.getMyProfile(jwt)
        );
    }

    @PutMapping
    public ResponseEntity<OwnerProviderProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProviderProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(
                providerProfileService.updateMyProfile(
                        jwt,
                        request
                )
        );
    }

    @PatchMapping("/details")
    public ResponseEntity<OwnerProviderProfileResponse> updateProfileDetails(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProviderProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(
                providerProfileService.updateProfileDetails(
                        jwt,
                        request
                )
        );
    }

    @PutMapping("/photo")
    public ResponseEntity<OwnerProviderProfileResponse> updateProfilePhoto(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam UUID mediaId
    ) {
        return ResponseEntity.ok(
                providerProfileService.updateProfilePhoto(
                        jwt,
                        mediaId
                )
        );
    }

    @GetMapping("/public")
    public ResponseEntity<List<PublicProviderProfileResponse>>
    getPublicProvidersByCategory(
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(
                providerProfileService.getPublicProvidersByCategory(
                        category
                )
        );
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<PublicProviderProfileResponse> getProvider(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                providerProfileService.getProvider(id)
        );
    }
}