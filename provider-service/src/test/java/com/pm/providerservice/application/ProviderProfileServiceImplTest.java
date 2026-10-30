package com.pm.providerservice.application;

import com.pm.providerservice.api.dto.OwnerProviderProfileResponse;
import com.pm.providerservice.api.dto.ProviderProfileCreateRequest;
import com.pm.providerservice.api.dto.ProviderProfileUpdateRequest;
import com.pm.providerservice.application.mapper.ProviderEventMapper;
import com.pm.providerservice.application.ports.OutboxDomainEventPublisher;
import com.pm.providerservice.application.service.impl.ProviderProfileServiceImpl;
import com.pm.providerservice.client.RatingClient;
import com.pm.providerservice.client.dto.ProviderRatingSummaryResponse;
import com.pm.providerservice.domain.ProviderProfile;
import com.pm.providerservice.exception.ProviderProfileNotFoundException;
import com.pm.providerservice.repository.ProviderProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.oauth2.jwt.Jwt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderProfileServiceImplTest {

    @Mock
    private ProviderProfileRepository repository;

    @Mock
    private RatingClient ratingClient;

    @Mock
    private OutboxDomainEventPublisher outboxEventPublisher;

    @Mock
    private ProviderEventMapper providerEventMapper;

    @InjectMocks
    private ProviderProfileServiceImpl service;

    private UUID providerId;
    private String authUserId;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        providerId = UUID.randomUUID();
        authUserId = UUID.randomUUID().toString();

        jwt = Jwt.withTokenValue("test-token")
                .subject(authUserId)
                .claim("email", "provider@example.com")
                .header("alg", "none")
                .build();
    }

    @Test
    void shouldCreateProviderProfileForAuthenticatedUser() {

        ProviderProfileCreateRequest request =
                new ProviderProfileCreateRequest(
                        "Emmanuel Plumbing",
                        "Plumbing"
                );

        ProviderProfile saved =
                new ProviderProfile(
                        authUserId,
                        "Emmanuel Plumbing",
                        "Plumbing",
                        "provider@example.com"
                );

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.empty());

        when(repository.save(any(ProviderProfile.class)))
                .thenReturn(saved);

        OwnerProviderProfileResponse response =
                service.createMyProfile(jwt, request);

        assertNotNull(response);
        assertEquals(
                "Emmanuel Plumbing",
                response.businessName()
        );
        assertEquals(
                "Plumbing",
                response.category()
        );
        assertEquals(
                "provider@example.com",
                response.email()
        );

        verify(repository).save(any(ProviderProfile.class));
        verify(providerEventMapper).toCreatedEvent(saved);
        verify(outboxEventPublisher).publish(any());
    }

    @Test
    void shouldReturnExistingProfileInsteadOfCreatingDuplicate() {

        ProviderProfile existing =
                new ProviderProfile(
                        authUserId,
                        "Existing Business",
                        "Electrical",
                        "provider@example.com"
                );

        ProviderProfileCreateRequest request =
                new ProviderProfileCreateRequest(
                        "New Business",
                        "Plumbing"
                );

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.of(existing));

        OwnerProviderProfileResponse response =
                service.createMyProfile(jwt, request);

        assertEquals(
                "Existing Business",
                response.businessName()
        );

        assertEquals(
                "Electrical",
                response.category()
        );

        verify(repository, never())
                .save(any(ProviderProfile.class));

        verify(providerEventMapper, never())
                .toCreatedEvent(any());
    }

    @Test
    void shouldUpdateOwnedProviderProfile() {

        ProviderProfile profile =
                new ProviderProfile(
                        authUserId,
                        "Old Business",
                        "Plumbing",
                        "provider@example.com"
                );

        ProviderProfileUpdateRequest request =
                new ProviderProfileUpdateRequest(
                        "Updated Business",
                        "Electrical",
                        "Professional electrical services",
                        "08012345678"
                );

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.of(profile));

        OwnerProviderProfileResponse response =
                service.updateMyProfile(jwt, request);

        assertEquals(
                "Updated Business",
                profile.getBusinessName()
        );

        assertEquals(
                "Electrical",
                profile.getCategory()
        );

        assertEquals(
                "Professional electrical services",
                profile.getDescription()
        );

        assertEquals(
                "08012345678",
                profile.getPhone()
        );

        assertEquals(
                "Updated Business",
                response.businessName()
        );

        verify(providerEventMapper)
                .toUpdatedEvent(profile);

        verify(outboxEventPublisher)
                .publish(any());
    }

    @Test
    void shouldUpdateProfilePhotoMediaReference() {

        UUID mediaId = UUID.randomUUID();

        ProviderProfile profile =
                new ProviderProfile(
                        authUserId,
                        "Business",
                        "Plumbing",
                        "provider@example.com"
                );

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.of(profile));

        OwnerProviderProfileResponse response =
                service.updateProfilePhoto(
                        jwt,
                        mediaId
                );

        assertEquals(
                mediaId,
                profile.getProfilePhotoMediaId()
        );

        assertEquals(
                mediaId,
                response.profilePhotoMediaId()
        );

        verify(providerEventMapper)
                .toUpdatedEvent(profile);

        verify(outboxEventPublisher)
                .publish(any());
    }

    @Test
    void shouldRejectProfileOperationWhenProfileDoesNotExist() {

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProviderProfileNotFoundException.class,
                () -> service.getMyProfile(jwt)
        );

        verify(repository)
                .findByAuthUserId(authUserId);
    }

    @Test
    void shouldUseRatingServiceSummaryWhenAvailable() {

        ProviderProfile profile =
                new ProviderProfile(
                        authUserId,
                        "Business",
                        "Plumbing",
                        "provider@example.com"
                );

        ReflectionTestUtils.setField(
                profile,
                "id",
                providerId
        );

        ProviderRatingSummaryResponse summary =
                new ProviderRatingSummaryResponse(
                        providerId,
                        4.7,
                        25
                );

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.of(profile));

        when(ratingClient.getProviderRatingSummary(providerId))
                .thenReturn(summary);

        OwnerProviderProfileResponse response =
                service.getMyProfile(jwt);

        assertEquals(
                4.7,
                response.averageRating()
        );

        assertEquals(
                25,
                response.ratingCount()
        );

        verify(ratingClient)
                .getProviderRatingSummary(providerId);
    }
    @Test
    void shouldFallBackToStoredRatingSnapshotWhenRatingServiceFails() {

        ProviderProfile profile =
                new ProviderProfile(
                        authUserId,
                        "Business",
                        "Plumbing",
                        "provider@example.com"
                );

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.of(profile));

        when(ratingClient.getProviderRatingSummary(any(UUID.class)))
                .thenThrow(new RuntimeException("Rating service unavailable"));

        OwnerProviderProfileResponse response =
                service.getMyProfile(jwt);

        assertEquals(
                0.0,
                response.averageRating()
        );

        assertEquals(
                0,
                response.ratingCount()
        );
    }
}