package com.pm.ratingservice.application.service.impl;

import com.pm.contracts.rating.CustomerRatedEvent;
import com.pm.contracts.rating.CustomerRatingDeletedEvent;
import com.pm.contracts.rating.CustomerRatingUpdatedEvent;
import com.pm.ratingservice.api.dto.request.CreateRatingRequest;
import com.pm.ratingservice.api.dto.request.UpdateRatingRequest;
import com.pm.ratingservice.api.dto.response.BookingRatingContextResponse;
import com.pm.ratingservice.api.dto.response.RatingResponse;
import com.pm.ratingservice.application.port.BookingGateway;
import com.pm.ratingservice.application.port.OutboxDomainEventPublisher;
import com.pm.ratingservice.application.port.RatingRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private BookingGateway bookingGateway;

    @Mock
    private SecurityService securityService;

    @Mock
    private OutboxDomainEventPublisher eventPublisher;

    private RatingServiceImpl ratingService;

    private UUID bookingId;
    private UUID customerId;
    private UUID providerId;
    private UUID ratingId;
    private UUID anotherUserId;

    @BeforeEach
    void setUp() {
        ratingService = new RatingServiceImpl(
                ratingRepository,
                bookingGateway,
                securityService,
                eventPublisher
        );

        bookingId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        ratingId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
    }

    // =========================================================
    // CREATE RATING
    // =========================================================

    @Test
    void shouldCreateRatingSuccessfully() {
        CreateRatingRequest request =
                new CreateRatingRequest(
                        bookingId,
                        5,
                        "Excellent service"
                );

        BookingRatingContextResponse booking =
                completedBooking();

        Rating savedRating = createRatingEntity(
                bookingId,
                customerId,
                providerId,
                5,
                "Excellent service"
        );

        RatingRepository.RatingStatistics statistics =
                new RatingRepository.RatingStatistics(
                        4.5,
                        10
                );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(bookingGateway.getRatingContext(bookingId))
                .thenReturn(booking);

        when(
                ratingRepository
                        .existsByBookingIdAndReviewerUserIdAndRevieweeUserId(
                                bookingId,
                                customerId,
                                providerId
                        )
        ).thenReturn(false);

        when(ratingRepository.save(any(Rating.class)))
                .thenReturn(savedRating);

        when(
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                )
        ).thenReturn(statistics);

        RatingResponse response =
                ratingService.createRating(request);

        assertEquals(
                ratingId,
                response.id()
        );

        assertEquals(
                bookingId,
                response.bookingId()
        );

        assertEquals(
                customerId,
                response.reviewerUserId()
        );

        assertEquals(
                providerId,
                response.revieweeUserId()
        );

        assertEquals(
                5,
                response.score()
        );

        assertEquals(
                "Excellent service",
                response.comment()
        );

        verify(ratingRepository).save(any(Rating.class));

        verify(eventPublisher).publish(
                any(CustomerRatedEvent.class)
        );
    }

    @Test
    void shouldUseProviderFromBookingWhenCreatingRating() {
        CreateRatingRequest request =
                new CreateRatingRequest(
                        bookingId,
                        4,
                        "Good work"
                );

        BookingRatingContextResponse booking =
                completedBooking();

        Rating savedRating = createRatingEntity(
                bookingId,
                customerId,
                providerId,
                4,
                "Good work"
        );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(bookingGateway.getRatingContext(bookingId))
                .thenReturn(booking);

        when(
                ratingRepository
                        .existsByBookingIdAndReviewerUserIdAndRevieweeUserId(
                                bookingId,
                                customerId,
                                providerId
                        )
        ).thenReturn(false);

        when(ratingRepository.save(any(Rating.class)))
                .thenReturn(savedRating);

        when(
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                )
        ).thenReturn(
                new RatingRepository.RatingStatistics(
                        4.0,
                        5
                )
        );

        ratingService.createRating(request);

        ArgumentCaptor<Rating> ratingCaptor =
                ArgumentCaptor.forClass(Rating.class);

        verify(ratingRepository).save(
                ratingCaptor.capture()
        );

        Rating rating = ratingCaptor.getValue();

        assertEquals(
                providerId,
                rating.getRevieweeUserId()
        );

        assertEquals(
                customerId,
                rating.getReviewerUserId()
        );

        assertEquals(
                ReviewerRole.CUSTOMER,
                rating.getReviewerRole()
        );

        assertEquals(
                ReviewerRole.PROVIDER,
                rating.getRevieweeRole()
        );
    }

    @Test
    void shouldPublishCustomerRatedEventAfterSuccessfulCreation() {
        CreateRatingRequest request =
                new CreateRatingRequest(
                        bookingId,
                        5,
                        "Excellent"
                );

        Rating savedRating = createRatingEntity(
                bookingId,
                customerId,
                providerId,
                5,
                "Excellent"
        );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(bookingGateway.getRatingContext(bookingId))
                .thenReturn(completedBooking());

        when(
                ratingRepository
                        .existsByBookingIdAndReviewerUserIdAndRevieweeUserId(
                                bookingId,
                                customerId,
                                providerId
                        )
        ).thenReturn(false);

        when(ratingRepository.save(any(Rating.class)))
                .thenReturn(savedRating);

        when(
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                )
        ).thenReturn(
                new RatingRepository.RatingStatistics(
                        4.7,
                        11
                )
        );

        ratingService.createRating(request);

        ArgumentCaptor<CustomerRatedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        CustomerRatedEvent.class
                );

        verify(eventPublisher).publish(
                eventCaptor.capture()
        );

        CustomerRatedEvent event =
                eventCaptor.getValue();

        assertEquals(
                bookingId,
                event.bookingId()
        );

        assertEquals(
                ratingId,
                event.ratingId()
        );

        assertEquals(
                providerId,
                event.providerId()
        );

        assertEquals(
                customerId,
                event.customerId()
        );

        assertEquals(
                5,
                event.rating()
        );

        assertEquals(
                4.7,
                event.averageRating()
        );

        assertEquals(
                11L,
                event.totalRatings()
        );

        assertEquals(
                1,
                event.eventVersion()
        );

        assertEquals(
                "CUSTOMER_RATED",
                event.eventType()
        );
    }

    // =========================================================
    // BOOKING ELIGIBILITY
    // =========================================================

    @Test
    void shouldRejectRatingWhenBookingDoesNotExist() {
        CreateRatingRequest request =
                new CreateRatingRequest(
                        bookingId,
                        5,
                        "Excellent"
                );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(bookingGateway.getRatingContext(bookingId))
                .thenReturn(null);

        assertThrows(
                BookingNotFoundException.class,
                () -> ratingService.createRating(request)
        );

        verify(ratingRepository, never())
                .save(any(Rating.class));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectRatingWhenBookingIsNotCompleted() {
        CreateRatingRequest request =
                new CreateRatingRequest(
                        bookingId,
                        5,
                        "Excellent"
                );

        BookingRatingContextResponse booking =
                new BookingRatingContextResponse(
                        bookingId,
                        customerId,
                        providerId,
                        false
                );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(bookingGateway.getRatingContext(bookingId))
                .thenReturn(booking);

        assertThrows(
                BookingNotCompletedException.class,
                () -> ratingService.createRating(request)
        );

        verify(ratingRepository, never())
                .save(any(Rating.class));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectRatingWhenAuthenticatedUserIsNotBookingCustomer() {
        CreateRatingRequest request =
                new CreateRatingRequest(
                        bookingId,
                        5,
                        "Excellent"
                );

        UUID unauthorizedUserId =
                anotherUserId;

        when(securityService.getAuthenticatedUserId())
                .thenReturn(unauthorizedUserId);

        when(bookingGateway.getRatingContext(bookingId))
                .thenReturn(completedBooking());

        assertThrows(
                UserNotBookingParticipantException.class,
                () -> ratingService.createRating(request)
        );

        verify(ratingRepository, never())
                .save(any(Rating.class));

        verify(
                ratingRepository,
                never()
        ).existsByBookingIdAndReviewerUserIdAndRevieweeUserId(
                any(UUID.class),
                any(UUID.class),
                any(UUID.class)
        );

        verifyNoInteractions(eventPublisher);
    }

    // =========================================================
    // DUPLICATE RATING
    // =========================================================

    @Test
    void shouldRejectDuplicateRating() {
        CreateRatingRequest request =
                new CreateRatingRequest(
                        bookingId,
                        5,
                        "Excellent"
                );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(bookingGateway.getRatingContext(bookingId))
                .thenReturn(completedBooking());

        when(
                ratingRepository
                        .existsByBookingIdAndReviewerUserIdAndRevieweeUserId(
                                bookingId,
                                customerId,
                                providerId
                        )
        ).thenReturn(true);

        assertThrows(
                DuplicateRatingException.class,
                () -> ratingService.createRating(request)
        );

        verify(ratingRepository, never())
                .save(any(Rating.class));

        verifyNoInteractions(eventPublisher);
    }

    // =========================================================
    // UPDATE RATING
    // =========================================================

    @Test
    void shouldUpdateRatingSuccessfully() {
        Rating rating = createRatingEntity(
                bookingId,
                customerId,
                providerId,
                3,
                "Original comment"
        );

        UpdateRatingRequest request =
                new UpdateRatingRequest(
                        5,
                        "Updated comment"
                );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(ratingRepository.findById(ratingId))
                .thenReturn(Optional.of(rating));

        when(ratingRepository.save(rating))
                .thenReturn(rating);

        when(
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                )
        ).thenReturn(
                new RatingRepository.RatingStatistics(
                        4.8,
                        10
                )
        );

        RatingResponse response =
                ratingService.updateRating(
                        ratingId,
                        request
                );

        assertEquals(
                5,
                rating.getScore()
        );

        assertEquals(
                "Updated comment",
                rating.getComment()
        );

        assertEquals(
                5,
                response.score()
        );

        assertEquals(
                "Updated comment",
                response.comment()
        );

        verify(ratingRepository).save(rating);

        verify(eventPublisher).publish(
                any(CustomerRatingUpdatedEvent.class)
        );
    }

    @Test
    void shouldRejectUpdateWhenRatingDoesNotExist() {
        UpdateRatingRequest request =
                new UpdateRatingRequest(
                        5,
                        "Updated"
                );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(ratingRepository.findById(ratingId))
                .thenReturn(Optional.empty());

        assertThrows(
                RatingNotFoundException.class,
                () -> ratingService.updateRating(
                        ratingId,
                        request
                )
        );

        verify(ratingRepository, never())
                .save(any(Rating.class));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectUpdateByAnotherUser() {
        Rating rating = createRatingEntity(
                bookingId,
                customerId,
                providerId,
                3,
                "Original"
        );

        UpdateRatingRequest request =
                new UpdateRatingRequest(
                        5,
                        "Attempted update"
                );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(anotherUserId);

        when(ratingRepository.findById(ratingId))
                .thenReturn(Optional.of(rating));

        assertThrows(
                UnauthorizedRatingUpdateException.class,
                () -> ratingService.updateRating(
                        ratingId,
                        request
                )
        );

        assertEquals(
                3,
                rating.getScore()
        );

        assertEquals(
                "Original",
                rating.getComment()
        );

        verify(ratingRepository, never())
                .save(any(Rating.class));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldPublishCustomerRatingUpdatedEventAfterSuccessfulUpdate() {
        Rating rating = createRatingEntity(
                bookingId,
                customerId,
                providerId,
                3,
                "Original"
        );

        UpdateRatingRequest request =
                new UpdateRatingRequest(
                        4,
                        "Updated"
                );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(ratingRepository.findById(ratingId))
                .thenReturn(Optional.of(rating));

        when(ratingRepository.save(rating))
                .thenReturn(rating);

        when(
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                )
        ).thenReturn(
                new RatingRepository.RatingStatistics(
                        4.2,
                        20
                )
        );

        ratingService.updateRating(
                ratingId,
                request
        );

        ArgumentCaptor<CustomerRatingUpdatedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        CustomerRatingUpdatedEvent.class
                );

        verify(eventPublisher).publish(
                eventCaptor.capture()
        );

        CustomerRatingUpdatedEvent event =
                eventCaptor.getValue();

        assertEquals(
                bookingId,
                event.bookingId()
        );

        assertEquals(
                ratingId,
                event.ratingId()
        );

        assertEquals(
                providerId,
                event.providerId()
        );

        assertEquals(
                customerId,
                event.customerId()
        );

        assertEquals(
                4,
                event.rating()
        );

        assertEquals(
                4.2,
                event.averageRating()
        );

        assertEquals(
                20L,
                event.totalRatings()
        );

        assertEquals(
                1,
                event.eventVersion()
        );

        assertEquals(
                "CUSTOMER_RATING_UPDATED",
                event.eventType()
        );
    }

    // =========================================================
    // DELETE RATING
    // =========================================================

    @Test
    void shouldDeleteRatingSuccessfully() {
        Rating rating = createRatingEntity(
                bookingId,
                customerId,
                providerId,
                5,
                "Excellent"
        );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(ratingRepository.findById(ratingId))
                .thenReturn(Optional.of(rating));

        when(
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                )
        ).thenReturn(
                new RatingRepository.RatingStatistics(
                        4.0,
                        9
                )
        );

        ratingService.deleteRating(ratingId);

        verify(ratingRepository).delete(rating);

        verify(eventPublisher).publish(
                any(CustomerRatingDeletedEvent.class)
        );
    }

    @Test
    void shouldRejectDeleteWhenRatingDoesNotExist() {
        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(ratingRepository.findById(ratingId))
                .thenReturn(Optional.empty());

        assertThrows(
                RatingNotFoundException.class,
                () -> ratingService.deleteRating(ratingId)
        );

        verify(ratingRepository, never())
                .delete(any(Rating.class));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectDeleteByAnotherUser() {
        Rating rating = createRatingEntity(
                bookingId,
                customerId,
                providerId,
                5,
                "Excellent"
        );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(anotherUserId);

        when(ratingRepository.findById(ratingId))
                .thenReturn(Optional.of(rating));

        assertThrows(
                UnauthorizedRatingDeleteException.class,
                () -> ratingService.deleteRating(ratingId)
        );

        verify(ratingRepository, never())
                .delete(any(Rating.class));

        verify(
                ratingRepository,
                never()
        ).getStatistics(
                any(UUID.class),
                any(ReviewerRole.class)
        );

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldPublishCustomerRatingDeletedEventAfterSuccessfulDelete() {
        Rating rating = createRatingEntity(
                bookingId,
                customerId,
                providerId,
                5,
                "Excellent"
        );

        when(securityService.getAuthenticatedUserId())
                .thenReturn(customerId);

        when(ratingRepository.findById(ratingId))
                .thenReturn(Optional.of(rating));

        when(
                ratingRepository.getStatistics(
                        providerId,
                        ReviewerRole.PROVIDER
                )
        ).thenReturn(
                new RatingRepository.RatingStatistics(
                        4.0,
                        9
                )
        );

        ratingService.deleteRating(ratingId);

        ArgumentCaptor<CustomerRatingDeletedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        CustomerRatingDeletedEvent.class
                );

        verify(eventPublisher).publish(
                eventCaptor.capture()
        );

        CustomerRatingDeletedEvent event =
                eventCaptor.getValue();

        assertEquals(
                bookingId,
                event.bookingId()
        );

        assertEquals(
                ratingId,
                event.ratingId()
        );

        assertEquals(
                providerId,
                event.providerId()
        );

        assertEquals(
                customerId,
                event.customerId()
        );

        /*
         * The current production implementation deliberately
         * publishes null for the rating after deletion.
         */
        assertEquals(
                null,
                event.rating()
        );

        assertEquals(
                4.0,
                event.averageRating()
        );

        assertEquals(
                9L,
                event.totalRatings()
        );

        assertEquals(
                1,
                event.eventVersion()
        );

        assertEquals(
                "CUSTOMER_RATING_DELETED",
                event.eventType()
        );
    }

    // =========================================================
    // TEST DATA HELPERS
    // =========================================================

    private BookingRatingContextResponse completedBooking() {
        return new BookingRatingContextResponse(
                bookingId,
                customerId,
                providerId,
                true
        );
    }

    private Rating createRatingEntity(
            UUID bookingId,
            UUID reviewerUserId,
            UUID revieweeUserId,
            int score,
            String comment
    ) {
        Rating rating =
                Rating.create(
                        bookingId,
                        reviewerUserId,
                        ReviewerRole.CUSTOMER,
                        revieweeUserId,
                        ReviewerRole.PROVIDER,
                        score,
                        comment
                );

        /*
         * JPA normally assigns this ID.
         * The service returns RatingResponse.from(saved),
         * so the unit test needs a deterministic ID.
         */
        setRatingId(rating, ratingId);

        return rating;
    }

    private void setRatingId(
            Rating rating,
            UUID id
    ) {
        try {
            var field =
                    Rating.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(rating, id);

        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(
                    "Could not set Rating ID for test",
                    ex
            );
        }
    }
}