package com.pm.bookingservice.application.service;

import com.pm.bookingservice.application.dto.query.BookingFilter;
import com.pm.bookingservice.application.dto.request.CreateBookingRequest;
import com.pm.bookingservice.application.dto.request.UpdateBookingRequest;
import com.pm.bookingservice.application.dto.response.*;
import com.pm.bookingservice.application.event.BookingEventFactory;
import com.pm.bookingservice.application.event.DomainEventPublisher;
import com.pm.bookingservice.application.mapper.BookingFilterMapper;
import com.pm.bookingservice.application.mapper.BookingMapper;
import com.pm.bookingservice.domain.exception.BookingAccessDeniedException;
import com.pm.bookingservice.domain.exception.BookingNotFoundException;
import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.BookingStatus;
import com.pm.bookingservice.domain.model.BookingStatusHistory;
import com.pm.bookingservice.domain.model.quote.BookingQuote;
import com.pm.bookingservice.domain.model.quote.QuoteStatus;
import com.pm.bookingservice.domain.repository.BookingRepository;
import com.pm.bookingservice.domain.repository.BookingStatusHistoryRepository;
import com.pm.bookingservice.domain.repository.quote.BookingQuoteRepository;
import com.pm.bookingservice.infrastructure.client.PaymentServiceClient;
import com.pm.bookingservice.infrastructure.client.ProviderServiceClient;
import com.pm.bookingservice.integration.dto.EscrowResponse;
import com.pm.bookingservice.integration.dto.ProviderServiceDetailsResponse;
import com.pm.bookingservice.integration.dto.ReleaseMaterialsRequest;
import com.pm.bookingservice.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository repository;
    private final ProviderServiceClient providerClient;
    private final SecurityService securityService;
    private final DomainEventPublisher publisher;
    private final BookingQuoteRepository bookingQuoteRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final BookingEventFactory bookingEventFactory;
    private final BookingStatusHistoryRepository statusHistoryRepository;
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);


    public BookingServiceImpl(BookingRepository repository,
                              ProviderServiceClient providerClient,
                              SecurityService securityService,
                              DomainEventPublisher publisher,
                              BookingQuoteRepository bookingQuoteRepository,
                              BookingStatusHistoryRepository statusHistoryRepository,
                              PaymentServiceClient paymentServiceClient,
                              BookingEventFactory bookingEventFactory
    ) {

        this.repository = repository;
        this.providerClient = providerClient;
        this.securityService = securityService;
        this.publisher = publisher;
        this.bookingQuoteRepository = bookingQuoteRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.paymentServiceClient = paymentServiceClient;
        this.bookingEventFactory=bookingEventFactory;

    }

    private Booking getBookingOrThrow(
            UUID bookingId
    ){

        return repository.findById(

                bookingId

        ).orElseThrow(

                ()->new BookingNotFoundException(

                        bookingId
                )
        );

    }
    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {

        log.info(
                "🔥 Creating booking started for providerServiceId={}",
                request.providerServiceId()
        );

        UUID customerId =
                securityService.getAuthenticatedUserId();

        log.info(
                "👤 Authenticated customerId={}",
                customerId
        );

        /*
         * STEP 1:
         * Fetch Provider Service
         */
        ProviderServiceDetailsResponse providerService;

        try {

            log.info(
                    "📡 Calling Provider Service for providerServiceId={}",
                    request.providerServiceId()
            );

            providerService = providerClient.getService(
                    request.providerServiceId()
            );

            log.info("✅ Provider Service response received.");

        } catch (Exception ex) {

            log.error(
                    "❌ Failed calling Provider Service for providerServiceId={}",
                    request.providerServiceId(),
                    ex
            );

            throw ex;

        }

        /*
         * STEP 2:
         * Create Booking Aggregate
         */
        Booking booking = Booking.create(

                customerId,

                providerService.providerAuthUserId(),

                providerService.id(),

                providerService.serviceName(),

                providerService.price(),

                request.description(),

                request.notes(),

                BookingMapper.toAddress(
                        request.address()
                ),

                request.scheduledStart(),

                request.scheduledEnd()

        );

        /*
         * STEP 3:
         * Persist Booking
         */
        Booking savedBooking =
                repository.save(booking);

        log.info(
                "💾 Booking saved. id={}",
                savedBooking.getId()
        );

        /*
         * STEP 4:
         * Record Initial Status
         */
        recordStatusChange(
                savedBooking,
                customerId,
                "Booking created"
        );

        log.info(
                "📝 Initial booking status recorded. bookingId={}, status={}",
                savedBooking.getId(),
                savedBooking.getStatus()
        );

        /*
         * STEP 5:
         * Publish Domain Event
         */
        publisher.publish(

                bookingEventFactory.bookingCreated(

                        savedBooking

                )

        );

        log.info(
                "📤 BookingCreatedEvent published. bookingId={}",
                savedBooking.getId()
        );

        log.info(
                "🎉 Booking creation completed successfully."
        );

        return BookingMapper.toResponse(
                savedBooking
        );

    }

    @Override
    public BookingResponse acceptJob(UUID bookingId) {

        UUID providerId =
                securityService.getAuthenticatedUserId();

        Booking booking =
                getBookingOrThrow(bookingId);

        validateProviderOwnership(
                booking,
                providerId
        );

        BookingStatus previousStatus =
                booking.getStatus();

        booking.accept();

        repository.save(booking);

        /*
         * Record status transition
         */
        recordStatusChange(
                booking,
                providerId,
                "Booking accepted by provider"
        );

        /*
         * Publish domain events
         */
        publisher.publish(
                bookingEventFactory.bookingAccepted(
                        booking
                )
        );

        publisher.publish(
                bookingEventFactory.statusChanged(
                        booking,
                        previousStatus
                )
        );

        return BookingMapper.toResponse(booking);

    }

    @Override
    public BookingResponse rejectJob(UUID bookingId) {

        UUID providerId =
                securityService.getAuthenticatedUserId();

        Booking booking =
                getBookingOrThrow(bookingId);

        validateProviderOwnership(
                booking,
                providerId
        );

        BookingStatus previousStatus =
                booking.getStatus();

        booking.reject();

        repository.save(booking);

        /*
         * Record status transition
         */
        recordStatusChange(
                booking,
                providerId,
                "Booking rejected by provider"
        );

        /*
         * Publish domain events
         */
        publisher.publish(
                bookingEventFactory.bookingRejected(
                        booking
                )
        );

        publisher.publish(
                bookingEventFactory.statusChanged(
                        booking,
                        previousStatus
                )
        );

        return BookingMapper.toResponse(booking);

    }

    @Override
    public BookingResponse startJob(UUID bookingId) {

        UUID providerId =
                securityService.getAuthenticatedUserId();

        Booking booking =
                getBookingOrThrow(bookingId);

        validateProviderOwnership(
                booking,
                providerId
        );

        BookingStatus previousStatus =
                booking.getStatus();

        booking.start();

        repository.save(booking);

        /*
         * Record status transition
         */
        recordStatusChange(
                booking,
                providerId,
                "Work started by provider"
        );

        /*
         * Publish domain events
         */
        publisher.publish(
                bookingEventFactory.bookingStarted(
                        booking
                )
        );

        publisher.publish(
                bookingEventFactory.statusChanged(
                        booking,
                        previousStatus
                )
        );

        return BookingMapper.toResponse(booking);

    }

    @Override
    public BookingResponse completeJob(UUID bookingId) {

        UUID providerId =
                securityService.getAuthenticatedUserId();

        Booking booking =
                getBookingOrThrow(bookingId);

        validateProviderOwnership(
                booking,
                providerId
        );

        BookingStatus previousStatus =
                booking.getStatus();

        booking.complete();

        repository.save(booking);

        /*
         * Record status transition
         */
        recordStatusChange(
                booking,
                providerId,
                "Work completed by provider"
        );

        /*
         * Publish domain events
         */
        publisher.publish(
                bookingEventFactory.bookingCompleted(
                        booking
                )
        );

        publisher.publish(
                bookingEventFactory.statusChanged(
                        booking,
                        previousStatus
                )
        );

        return BookingMapper.toResponse(booking);

    }
    @Override
    public BookingResponse cancelBooking(UUID bookingId) {

        UUID customerId =
                securityService.getAuthenticatedUserId();

        Booking booking =
                getBookingOrThrow(bookingId);

        validateCustomerOwnership(
                booking,
                customerId
        );

        BookingStatus previousStatus =
                booking.getStatus();

        booking.cancel();

        repository.save(booking);

        /*
         * Record status transition
         */
        recordStatusChange(
                booking,
                customerId,
                "Booking cancelled by customer"
        );

        /*
         * Publish domain events
         */
        publisher.publish(
                bookingEventFactory.bookingCancelled(
                        booking
                )
        );

        publisher.publish(
                bookingEventFactory.statusChanged(
                        booking,
                        previousStatus
                )
        );

        return BookingMapper.toResponse(booking);

    }
    @Override
    public BookingResponse updateBooking(
            UUID bookingId,
            UpdateBookingRequest request
    ) {

        UUID customerId =
                securityService.getAuthenticatedUserId();

        Booking booking =
                getBookingOrThrow(bookingId);

        validateCustomerOwnership(
                booking,
                customerId
        );

        booking.update(

                request.description(),

                request.notes(),

                BookingMapper.toAddress(
                        request.address()
                ),

                request.scheduledStart(),

                request.scheduledEnd()

        );

        repository.save(booking);

        /*
         * Publish booking updated event.
         *
         * No status history is recorded because
         * the booking status did not change.
         */
        publisher.publish(

                bookingEventFactory.bookingUpdated(

                        booking

                )

        );

        return BookingMapper.toResponse(booking);

    }

    @Override
    public BookingResponse getBooking(UUID bookingId) {

        UUID currentUser =
                securityService.getAuthenticatedUserId();

        Booking booking =
                getBookingOrThrow(bookingId);

        boolean owner =
                booking.getUserId().equals(currentUser);

        boolean provider =
                booking.getProviderUserId().equals(currentUser);

        if (!owner && !provider) {

            throw new BookingAccessDeniedException();

        }

        return BookingMapper.toResponse(booking);

    }

    @Override
    @org.springframework.transaction.annotation.Transactional(
            propagation =
                    org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED
    )
    public MaterialReleaseResponse requestMaterialsRelease(UUID bookingId) {

        UUID providerId = securityService.getAuthenticatedUserId();

        Booking booking = getBookingOrThrow(bookingId);

        validateProviderOwnership(booking, providerId);

        BookingQuote quote = bookingQuoteRepository
                .findTopByBookingIdAndStatusOrderByCreatedAtDesc(
                        bookingId,
                        QuoteStatus.ACCEPTED
                )
                .orElseThrow(() -> new IllegalStateException(
                        "No accepted quote found for booking: " + bookingId
                ));

        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Quote has not been accepted."
            );
        }

        /*
         * Do not reject a retry merely because the local quote already
         * records release. Payment Service checks the original quote ID
         * and amount and returns the existing release for a valid retry.
         */
        if (!quote.isMaterialsReleased()
                && !booking.getStatus().isPaymentCompleted()) {
            throw new IllegalStateException(
                    "Materials cannot be released until payment is completed."
            );
        }

        EscrowResponse escrow = paymentServiceClient.releaseMaterials(
                booking.getId(),
                new ReleaseMaterialsRequest(
                        quote.getId(),
                        quote.getMaterialsAmount()
                )
        );

        if (escrow == null
                || !booking.getId().equals(escrow.bookingId())) {
            throw new IllegalStateException(
                    "Payment Service did not return a valid escrow response."
            );
        }

        /*
         * Booking state is still updated by MATERIALS_RELEASED,
         * not by this synchronous HTTP response.
         */
        return new MaterialReleaseResponse(
                booking.getId(),
                quote.getId(),
                booking.getStatus().isMaterialsReleased(),
                escrow
        );
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(
            propagation =
                    org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED
    )
    public CompletionConfirmationResponse confirmCompletion(UUID bookingId) {
        UUID customerId = securityService.getAuthenticatedUserId();

        Booking booking = getBookingOrThrow(bookingId);

        validateCustomerOwnership(booking, customerId);

        /*
         * Allow COMPLETION_CONFIRMED so a repeated request can retrieve
         * Payment Service's existing labour-release result.
         *
         * Reject every other state before requesting a financial operation.
         */
        BookingStatus currentStatus = booking.getStatus();

        if (currentStatus != BookingStatus.COMPLETED
                && currentStatus != BookingStatus.COMPLETION_CONFIRMED) {
            throw new IllegalStateException(
                    "Completion can only be confirmed for a completed booking. "
                            + "Current status: " + currentStatus
            );
        }

        /*
         * Request labour release from Payment Service.
         *
         * Payment Service must make this operation idempotent. If the request
         * times out after the release commits, retrying must not release the
         * provider's funds a second time.
         */
        EscrowResponse escrow = paymentServiceClient.releaseLabour(
                booking.getId()
        );

        /*
         * Validate the response, but do not treat the synchronous HTTP response
         * as authority to transition Booking state.
         */
        if (escrow == null
                || escrow.escrowId() == null
                || !booking.getId().equals(escrow.bookingId())
                || escrow.remainingAmount() == null
                || escrow.remainingAmount().compareTo(
                java.math.BigDecimal.ZERO
        ) != 0
                || !"FULLY_RELEASED".equals(escrow.status())) {
            throw new IllegalStateException(
                    "Payment Service did not confirm a fully released escrow "
                            + "for booking " + booking.getId()
            );
        }

        /*
         * LABOUR_RELEASED is responsible for:
         * - transitioning Booking to COMPLETION_CONFIRMED;
         * - recording status history;
         * - recording the processed event idempotently;
         * - publishing resulting Booking events through the transactional outbox.
         *
         * Do not change Booking state or publish completion events here.
         */
        return new CompletionConfirmationResponse(
                BookingMapper.toResponse(booking),
                escrow
        );
    }


    @Override
    public List<BookingStatusHistoryResponse> getBookingStatusHistory(
            UUID bookingId
    ) {

        UUID currentUser =
                securityService.getAuthenticatedUserId();

        Booking booking =
                getBookingOrThrow(bookingId);

        boolean owner =
                booking.getUserId().equals(currentUser);

        boolean provider =
                booking.getProviderUserId().equals(currentUser);

        if (!owner && !provider) {

            throw new BookingAccessDeniedException();

        }

        return statusHistoryRepository
                .findByBookingIdOrderByChangedAtAsc(bookingId)
                .stream()
                .map(history ->
                        new BookingStatusHistoryResponse(

                                history.getStatus(),

                                history.getChangedAt(),

                                history.getChangedBy(),

                                history.getNote()

                        )
                )
                .toList();
    }

    @Override
    public Page<BookingResponse> listClientBookings(
            BookingFilter filter,
            Pageable pageable
    ) {

        UUID customerId =
                securityService.getAuthenticatedUserId();

        if (filter == BookingFilter.ALL) {

            return repository.findByUserId(
                    customerId,
                    pageable
            ).map(BookingMapper::toResponse);

        }

        return repository.findByUserIdAndStatusIn(
                customerId,
                BookingFilterMapper.toStatuses(filter),
                pageable
        ).map(BookingMapper::toResponse);

    }

    @Override
    public List<BookingResponse> listRecentClientBookings() {

        UUID customerId =
                securityService.getAuthenticatedUserId();

        return repository
                .findTop5ByUserIdOrderByCreatedAtDesc(
                        customerId
                )
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Override
    public Page<BookingResponse> listProviderJobs(
            Pageable pageable
    ) {
        UUID provider = securityService.getAuthenticatedUserId();

        return repository.findByProviderUserId(
                provider,
                pageable

        ).map(BookingMapper::toResponse);


    }

    @Override
    public Page<BookingResponse> listAvailableJobs(
            Pageable pageable
    ) {
        UUID provider = securityService.getAuthenticatedUserId();

        return repository.findByProviderUserIdAndStatus(
                provider,
                BookingStatus.REQUESTED,
                pageable

        ).map(BookingMapper::toResponse);

      }

    @Override
    public BookingSummaryResponse getClientBookingSummary() {

        UUID customerId =
                securityService.getAuthenticatedUserId();

        long total =
                repository.countByUserId(customerId);

        long awaitingQuote =
                repository.countByUserIdAndStatusIn(
                        customerId,
                        BookingFilter.AWAITING_QUOTE.statuses()
                );

        long quoted =
                repository.countByUserIdAndStatusIn(
                        customerId,
                        BookingFilter.QUOTED.statuses()
                );

        long inProgress =
                repository.countByUserIdAndStatusIn(
                        customerId,
                        BookingFilter.IN_PROGRESS.statuses()
                );

        long completed =
                repository.countByUserIdAndStatusIn(
                        customerId,
                        BookingFilter.COMPLETED.statuses()
                );

        long cancelled =
                repository.countByUserIdAndStatusIn(
                        customerId,
                        BookingFilter.CANCELLED.statuses()
                );

        return new BookingSummaryResponse(
                total,
                awaitingQuote,
                quoted,
                inProgress,
                completed,
                cancelled
        );
    }

    @Override
    public List<BookingResponse> listUpcomingClientBookings() {

        UUID customerId =
                securityService.getAuthenticatedUserId();

        List<Booking> upcoming =
                repository
                        .findByUserIdAndScheduledStartAfterAndStatusNotInOrderByScheduledStartAsc(
                                customerId,
                                LocalDateTime.now(),
                                Set.of(
                                        BookingStatus.COMPLETED,
                                        BookingStatus.COMPLETION_CONFIRMED,
                                        BookingStatus.CANCELLED
                                )
                        );

        return upcoming.stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingRatingContextResponse getRatingContext(
            UUID bookingId
    ) {

        Booking booking =
                repository.findById(bookingId)
                        .orElseThrow(
                                () -> new BookingNotFoundException(
                                        bookingId
                                )
                        );

        return new BookingRatingContextResponse(

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId(),

                booking.getStatus()
                        == BookingStatus.COMPLETION_CONFIRMED

        );
    }

    private void recordStatusChange(
            Booking booking,
            UUID changedBy,
            String note
    ) {

        statusHistoryRepository.save(

                BookingStatusHistory.create(

                        booking.getId(),

                        booking.getStatus(),

                        changedBy,

                        note

                )

        );
    }

    private void validateCustomerOwnership(Booking booking, UUID customerId) {

        if (!booking.getUserId().equals(customerId)) {

            throw new BookingAccessDeniedException(

                    "Booking does not belong to customer"

            );
        }
    }

    private void validateProviderOwnership(Booking booking, UUID providerId) {

        if (!booking.getProviderUserId().equals(providerId)) {

            throw new BookingAccessDeniedException(

                    "Booking does not belong to provider"

            );
        }
    }

}