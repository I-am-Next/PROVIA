package com.pm.providerservice.application.service.impl;

import com.pm.providerservice.api.dto.OwnerProviderProfileResponse;
import com.pm.providerservice.api.dto.ProviderProfileCreateRequest;
import com.pm.providerservice.api.dto.ProviderProfileUpdateRequest;
import com.pm.providerservice.api.dto.PublicProviderProfileResponse;
import com.pm.providerservice.application.mapper.ProviderEventMapper;
import com.pm.providerservice.application.ports.OutboxDomainEventPublisher;
import com.pm.providerservice.application.service.ProviderProfileService;
import com.pm.providerservice.client.RatingClient;
import com.pm.providerservice.domain.ProviderProfile;
import com.pm.providerservice.exception.ProviderProfileNotFoundException;
import com.pm.providerservice.repository.ProviderProfileRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProviderProfileServiceImpl
        implements ProviderProfileService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ProviderProfileServiceImpl.class
            );

    private final ProviderProfileRepository repository;
    private final RatingClient ratingClient;
    private final OutboxDomainEventPublisher outboxEventPublisher;
    private final ProviderEventMapper providerEventMapper;

    public ProviderProfileServiceImpl(
            ProviderProfileRepository repository,
            RatingClient ratingClient,
            OutboxDomainEventPublisher outboxEventPublisher,
            ProviderEventMapper providerEventMapper
    ) {
        this.repository = repository;
        this.ratingClient = ratingClient;
        this.outboxEventPublisher = outboxEventPublisher;
        this.providerEventMapper = providerEventMapper;
    }

    @Override
    public OwnerProviderProfileResponse getMyProfile(
            Jwt jwt
    ) {

        ProviderProfile profile =
                getProfileByAuthUserId(
                        jwt.getSubject()
                );

        return buildOwnerResponse(profile);
    }

    @Override
    public OwnerProviderProfileResponse createMyProfile(
            Jwt jwt,
            ProviderProfileCreateRequest request
    ) {

        String authUserId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        ProviderProfile existing =
                repository.findByAuthUserId(authUserId)
                        .orElse(null);

        if (existing != null) {
            return buildOwnerResponse(existing);
        }

        ProviderProfile profile =
                new ProviderProfile(
                        authUserId,
                        request.businessName(),
                        request.category(),
                        email
                );

        ProviderProfile saved =
                repository.save(profile);

        outboxEventPublisher.publish(
                providerEventMapper.toCreatedEvent(saved)
        );

        return buildOwnerResponse(saved);
    }

    @Override
    public OwnerProviderProfileResponse updateMyProfile(
            Jwt jwt,
            ProviderProfileUpdateRequest request
    ) {

        ProviderProfile profile =
                getProfileByAuthUserId(
                        jwt.getSubject()
                );

        profile.updateBusinessName(
                request.businessName()
        );

        profile.updateCategory(
                request.category()
        );

        if (request.description() != null) {
            profile.updateDescription(
                    request.description()
            );
        }

        if (request.phone() != null) {
            profile.updatePhone(
                    request.phone()
            );
        }

        outboxEventPublisher.publish(
                providerEventMapper.toUpdatedEvent(profile)
        );

        return buildOwnerResponse(profile);
    }

    @Override
    public OwnerProviderProfileResponse updateProfileDetails(
            Jwt jwt,
            ProviderProfileUpdateRequest request
    ) {

        ProviderProfile profile =
                getProfileByAuthUserId(
                        jwt.getSubject()
                );

        if (request.description() != null) {
            profile.updateDescription(
                    request.description()
            );
        }

        if (request.phone() != null) {
            profile.updatePhone(
                    request.phone()
            );
        }

        outboxEventPublisher.publish(
                providerEventMapper.toUpdatedEvent(profile)
        );

        return buildOwnerResponse(profile);
    }

    @Override
    public OwnerProviderProfileResponse updateProfilePhoto(
            Jwt jwt,
            UUID mediaId
    ) {

        ProviderProfile profile =
                getProfileByAuthUserId(
                        jwt.getSubject()
                );

        profile.updateProfilePhoto(mediaId);

        outboxEventPublisher.publish(
                providerEventMapper.toUpdatedEvent(profile)
        );

        return buildOwnerResponse(profile);
    }

    @Override
    public PublicProviderProfileResponse getProvider(
            UUID id
    ) {

        ProviderProfile profile =
                repository.findById(id)
                        .orElseThrow(
                                ProviderProfileNotFoundException::new
                        );

        return PublicProviderProfileResponse.from(
                profile
        );
    }

    @Override
    public List<PublicProviderProfileResponse>
    getPublicProvidersByCategory(
            String category
    ) {

        return repository
                .findByCategory(category)
                .stream()
                .map(PublicProviderProfileResponse::from)
                .toList();
    }

    private ProviderProfile getProfileByAuthUserId(
            String authUserId
    ) {

        return repository.findByAuthUserId(authUserId)
                .orElseThrow(
                        ProviderProfileNotFoundException::new
                );
    }

    private OwnerProviderProfileResponse buildOwnerResponse(
            ProviderProfile profile
    ) {

        try {

            var summary =
                    ratingClient.getProviderRatingSummary(
                            profile.getId()
                    );

            if (summary != null) {

                return OwnerProviderProfileResponse.from(
                        profile,
                        summary
                );
            }

        } catch (Exception ex) {

            /*
             * Rating is an external read dependency.
             * The provider profile itself remains available
             * using the locally stored snapshot.
             */
            log.warn(
                    "Rating summary unavailable for provider {}. " +
                            "Using stored rating snapshot.",
                    profile.getId()
            );
        }

        return OwnerProviderProfileResponse.from(
                profile
        );
    }
}