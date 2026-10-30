package com.pm.ratingservice.application.service.impl;

import com.pm.contracts.rating.CustomerRatedEvent;
import com.pm.contracts.rating.CustomerRatingDeletedEvent;
import com.pm.contracts.rating.CustomerRatingUpdatedEvent;
import com.pm.ratingservice.api.dto.request.CreateRatingRequest;
import com.pm.ratingservice.api.dto.request.UpdateRatingRequest;
import com.pm.ratingservice.api.dto.response.BookingRatingContextResponse;
import com.pm.ratingservice.api.dto.response.ProviderRatingSummaryResponse;
import com.pm.ratingservice.api.dto.response.ProviderReviewsResponse;
import com.pm.ratingservice.api.dto.response.RatingResponse;
import com.pm.ratingservice.application.port.BookingGateway;
import com.pm.ratingservice.application.port.OutboxDomainEventPublisher;
import com.pm.ratingservice.application.port.RatingRepository;
import com.pm.ratingservice.application.service.RatingService;
import com.pm.ratingservice.domain.entity.Rating;
import com.pm.ratingservice.domain.enums.ReviewerRole;
import com.pm.ratingservice.domain.exception.BookingNotCompletedException;
import com.pm.ratingservice.domain.exception.BookingNotFoundException;
import com.pm.ratingservice.domain.exception.DuplicateRatingException;
import com.pm.ratingservice.domain.exception.RatingNotFoundException;
import com.pm.ratingservice.domain.exception.UnauthorizedRatingDeleteException;
import com.pm.ratingservice.domain.exception.UnauthorizedRatingUpdateException;
import com.pm.ratingservice.domain.exception.UserNotBookingParticipantException;
import com.pm.ratingservice.security.SecurityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final BookingGateway bookingGateway;
    private final SecurityService securityService;
    private final OutboxDomainEventPublisher eventPublisher;

    public RatingServiceImpl(
            RatingRepository ratingRepository,
            BookingGateway bookingGateway,
            SecurityService securityService,
            OutboxDomainEventPublisher eventPublisher
    ) {
        this.ratingRepository = ratingRepository;
        this.bookingGateway = bookingGateway;
        this.securityService = securityService;
        this.eventPublisher = eventPublisher;
    }

    // =========================================================
    // CREATE RATING
    // =========================================================

    @Override
    public RatingResponse createRating(
            CreateRatingRequest request
    ) {

        UUID reviewerUserId =
                securityService.getAuthenticatedUserId();

        // -----------------------------------------------------
        // GET BOOKING CONTEXT
        // -----------------------------------------------------

        BookingRatingContextResponse booking =
                bookingGateway.getRatingContext(
                        request.bookingId()
                );

        if (booking == null) {

            throw new BookingNotFoundException(
                    request.bookingId()
            );
        }

        // -----------------------------------------------------
        // VALIDATE BOOKING COMPLETION
        // -----------------------------------------------------

        if (!booking.completionConfirmed()) {

            throw new BookingNotCompletedException();
        }

        // -----------------------------------------------------
        // VALIDATE CUSTOMER OWNERSHIP
        // -----------------------------------------------------

        if (!booking.customerId().equals(reviewerUserId)) {

            throw new UserNotBookingParticipantException();
        }

        // -----------------------------------------------------
        // DETERMINE PROVIDER
        // -----------------------------------------------------

        UUID providerId =
                booking.providerId();

        // -----------------------------------------------------
        // CHECK DUPLICATE
        // -----------------------------------------------------

        boolean alreadyRated =
                ratingRepository
                        .existsByBookingIdAndReviewerUserIdAndRevieweeUserId(
                                request.bookingId(),
                                reviewerUserId,
                                providerId
                        );

        if (alreadyRated) {

            throw new DuplicateRatingException();
        }

        // -----------------------------------------------------
        // CREATE RATING
        // -----------------------------------------------------

        Rating rating =
                Rating.create(
                        request.bookingId(),
                        reviewerUserId,
                        ReviewerRole.CUSTOMER,
                        providerId,
                        ReviewerRole.PROVIDER,
                        request.score(),
                        request.comment()
                );

        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        Rating saved =
                ratingRepository.save(rating);

        // -----------------------------------------------------
        // GET NEW PROVIDER STATISTICS
        // -----------------------------------------------------

        RatingRepository.RatingStatistics statistics =
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                );

        // -----------------------------------------------------
        // PUBLISH EVENT
        // -----------------------------------------------------

        CustomerRatedEvent event =
                new CustomerRatedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        saved.getBookingId(),
                        saved.getId(),
                        providerId,
                        reviewerUserId,
                        saved.getScore(),
                        statistics.averageRating(),
                        statistics.totalRatings()
                );

        eventPublisher.publish(event);

        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        return RatingResponse.from(saved);
    }

    // =========================================================
    // GET RATING
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public RatingResponse getRating(
            UUID ratingId
    ) {

        Rating rating =
                ratingRepository.findById(ratingId)
                        .orElseThrow(
                                () -> new RatingNotFoundException(
                                        ratingId
                                )
                        );

        return RatingResponse.from(rating);
    }

    // =========================================================
    // UPDATE RATING
    // =========================================================

    @Override
    public RatingResponse updateRating(
            UUID ratingId,
            UpdateRatingRequest request
    ) {

        UUID reviewerUserId =
                securityService.getAuthenticatedUserId();

        // -----------------------------------------------------
        // FIND RATING
        // -----------------------------------------------------

        Rating rating =
                ratingRepository.findById(ratingId)
                        .orElseThrow(
                                () -> new RatingNotFoundException(
                                        ratingId
                                )
                        );

        // -----------------------------------------------------
        // VALIDATE OWNER
        // -----------------------------------------------------

        if (!rating.getReviewerUserId()
                .equals(reviewerUserId)) {

            throw new UnauthorizedRatingUpdateException();
        }

        // -----------------------------------------------------
        // UPDATE DOMAIN ENTITY
        // -----------------------------------------------------

        rating.update(
                request.score(),
                request.comment()
        );

        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        Rating updated =
                ratingRepository.save(rating);

        // -----------------------------------------------------
        // GET NEW PROVIDER STATISTICS
        // -----------------------------------------------------

        UUID providerId =
                updated.getRevieweeUserId();

        RatingRepository.RatingStatistics statistics =
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                );

        // -----------------------------------------------------
        // PUBLISH UPDATE EVENT
        // -----------------------------------------------------

        CustomerRatingUpdatedEvent event =
                new CustomerRatingUpdatedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        updated.getBookingId(),
                        updated.getId(),
                        providerId,
                        reviewerUserId,
                        updated.getScore(),
                        statistics.averageRating(),
                        statistics.totalRatings()
                );

        eventPublisher.publish(event);

        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        return RatingResponse.from(updated);
    }

    // =========================================================
    // DELETE RATING
    // =========================================================

    @Override
    public void deleteRating(
            UUID ratingId
    ) {

        UUID reviewerUserId =
                securityService.getAuthenticatedUserId();

        // -----------------------------------------------------
        // FIND RATING
        // -----------------------------------------------------

        Rating rating =
                ratingRepository.findById(ratingId)
                        .orElseThrow(
                                () -> new RatingNotFoundException(
                                        ratingId
                                )
                        );

        // -----------------------------------------------------
        // VALIDATE OWNER
        // -----------------------------------------------------

        if (!rating.getReviewerUserId()
                .equals(reviewerUserId)) {

            throw new UnauthorizedRatingDeleteException();
        }

        // -----------------------------------------------------
        // CAPTURE PROVIDER BEFORE DELETE
        // -----------------------------------------------------

        UUID providerId =
                rating.getRevieweeUserId();

        UUID bookingId =
                rating.getBookingId();

        // -----------------------------------------------------
        // DELETE
        // -----------------------------------------------------

        ratingRepository.delete(rating);

        // -----------------------------------------------------
        // GET NEW PROVIDER STATISTICS
        // -----------------------------------------------------

        RatingRepository.RatingStatistics statistics =
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                );

        // -----------------------------------------------------
        // PUBLISH DELETE EVENT
        // -----------------------------------------------------

        CustomerRatingDeletedEvent event =
                new CustomerRatingDeletedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        bookingId,
                        ratingId,
                        providerId,
                        reviewerUserId,
                        null,
                        statistics.averageRating(),
                        statistics.totalRatings()
                );

        eventPublisher.publish(event);
    }

    // =========================================================
    // GET RATING BY BOOKING
    // =========================================================

    @Override
    public RatingResponse getRatingByBookingId(
            UUID bookingId
    ) {

        Rating rating =
                ratingRepository
                        .findByBookingIdAndReviewerUserId(
                                bookingId,
                                securityService.getAuthenticatedUserId()
                        )
                        .orElseThrow(
                                () -> new RatingNotFoundException(
                                        bookingId
                                )
                        );

        return RatingResponse.from(rating);
    }

    // =========================================================
    // PROVIDER RATING SUMMARY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ProviderRatingSummaryResponse
    getProviderRatingSummary(
            UUID providerId
    ) {

        RatingRepository.RatingStatistics statistics =
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                );

        return new ProviderRatingSummaryResponse(
                providerId,
                statistics.averageRating(),
                statistics.totalRatings()
        );
    }

    // =========================================================
    // PROVIDER REVIEWS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ProviderReviewsResponse getProviderReviews(
            UUID providerId,
            Pageable pageable
    ) {

        RatingRepository.RatingStatistics statistics =
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                );

        Page<RatingResponse> reviews =
                ratingRepository
                        .findByRevieweeUserIdAndRevieweeRole(
                                providerId,
                                ReviewerRole.PROVIDER,
                                pageable
                        )
                        .map(RatingResponse::from);

        return ProviderReviewsResponse.from(
                providerId,
                statistics.averageRating(),
                statistics.totalRatings(),
                reviews
        );
    }
}
