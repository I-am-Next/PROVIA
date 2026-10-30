package com.pm.providerservice.application.service;

import com.pm.providerservice.api.dto.request.CreateProviderServiceRequest;
import com.pm.providerservice.api.dto.request.UpdateProviderServiceRequest;
import com.pm.providerservice.api.dto.response.ProviderServiceDetailsResponse;
import com.pm.providerservice.api.dto.response.ProviderServiceResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

public interface ProviderServiceService {

    ProviderServiceResponse createService(
            Jwt jwt,
            CreateProviderServiceRequest request
    );

    ProviderServiceResponse updateService(
            Jwt jwt,
            UUID serviceId,
            UpdateProviderServiceRequest request
    );

    void deleteService(
            Jwt jwt,
            UUID serviceId
    );

    ProviderServiceResponse toggleActive(
            Jwt jwt,
            UUID serviceId
    );

    List<ProviderServiceResponse> getProviderServices(
            UUID providerProfileId
    );

    List<ProviderServiceResponse> getMyServices(
            Jwt jwt
    );

    ProviderServiceDetailsResponse getService(
            UUID id
    );
}