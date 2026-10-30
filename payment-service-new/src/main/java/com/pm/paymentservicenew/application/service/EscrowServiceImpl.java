package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.response.EscrowResponse;
import com.pm.paymentservicenew.application.event.DomainEventPublisher;
import com.pm.paymentservicenew.application.factory.EscrowEventFactory;
import com.pm.paymentservicenew.application.factory.EscrowReleaseFactory;
import com.pm.paymentservicenew.application.mapper.EscrowMapper;
import com.pm.paymentservicenew.domain.exception.EscrowNotFoundException;
import com.pm.paymentservicenew.domain.model.escrow.Escrow;
import com.pm.paymentservicenew.domain.model.escrow.EscrowRelease;
import com.pm.paymentservicenew.domain.model.escrow.EscrowReleaseType;
import com.pm.paymentservicenew.domain.repository.EscrowReleaseRepository;
import com.pm.paymentservicenew.domain.repository.EscrowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EscrowServiceImpl implements EscrowService {

    private final EscrowRepository escrowRepository;
    private final EscrowReleaseRepository escrowReleaseRepository;
    private final WalletService walletService;
    private final DomainEventPublisher domainEventPublisher;
    private final EscrowEventFactory escrowEventFactory;
    private final EscrowReleaseFactory escrowReleaseFactory;

    public EscrowServiceImpl(
            EscrowRepository escrowRepository,
            EscrowReleaseRepository escrowReleaseRepository,
            WalletService walletService,
            DomainEventPublisher domainEventPublisher,
            EscrowEventFactory escrowEventFactory,
            EscrowReleaseFactory escrowReleaseFactory
    ) {
        this.escrowRepository = escrowRepository;
        this.escrowReleaseRepository = escrowReleaseRepository;
        this.walletService = walletService;
        this.domainEventPublisher = domainEventPublisher;
        this.escrowEventFactory = escrowEventFactory;
        this.escrowReleaseFactory = escrowReleaseFactory;
    }


    @Override
    public EscrowResponse releaseMaterials(
            UUID bookingId,
            UUID quoteId,
            BigDecimal amount
    ) {
        if (bookingId == null) {
            throw new IllegalArgumentException(
                    "Booking ID must not be null."
            );
        }

        if (quoteId == null) {
            throw new IllegalArgumentException(
                    "Quote ID must not be null."
            );
        }

        validatePositiveAmount(amount);

        Escrow escrow = getEscrowByBookingIdForUpdate(bookingId);

        List<EscrowRelease> existingReleases =
                escrowReleaseRepository.findByBookingIdAndReleaseType(
                        bookingId,
                        EscrowReleaseType.MATERIALS
                );

        if (!existingReleases.isEmpty()) {
            if (existingReleases.size() > 1) {
                throw new IllegalStateException(
                        "Multiple materials releases already exist for booking "
                                + bookingId
                );
            }

            EscrowRelease existing = existingReleases.getFirst();

            if (!quoteId.equals(existing.getQuoteId())) {
                throw new IllegalStateException(
                        "Materials release retry uses a different quote ID."
                );
            }

            if (existing.getAmount().compareTo(amount) != 0) {
                throw new IllegalStateException(
                        "Materials release retry uses a different amount."
                );
            }

            return EscrowMapper.toResponse(escrow);
        }

        escrow.release(amount, EscrowReleaseType.MATERIALS);
        escrowRepository.save(escrow);

        EscrowRelease release = escrowReleaseRepository.save(
                escrowReleaseFactory.materialsRelease(
                        escrow,
                        quoteId,
                        amount
                )
        );

        walletService.creditWallet(
                escrow.getProviderId(),
                release.getId(),
                escrow.getId(),
                amount,
                "Materials advance"
        );

        domainEventPublisher.publish(
                escrowEventFactory.materialsReleased(
                        escrow,
                        quoteId,
                        amount
                )
        );

        return EscrowMapper.toResponse(escrow);
    }



    @Override
    public EscrowResponse releaseLabour(UUID bookingId) {
        validateBookingId(bookingId);

        /*
         * Serialize labour-release attempts for this booking.
         * The lock remains held until the transaction commits or rolls back.
         */
        Escrow escrow = getEscrowByBookingIdForUpdate(bookingId);

        /*
         * Check for an existing operation BEFORE calculating the remaining
         * amount. After a successful release, the remaining amount is zero.
         */
        List<EscrowRelease> existingReleases =
                escrowReleaseRepository.findByBookingIdAndReleaseType(
                        bookingId,
                        EscrowReleaseType.LABOUR
                );

        if (existingReleases.size() > 1) {
            throw new IllegalStateException(
                    "Multiple labour releases already exist for booking "
                            + bookingId
            );
        }

        if (!existingReleases.isEmpty()) {
            EscrowRelease existing = existingReleases.getFirst();

            /*
             * A previously committed labour release must correspond to
             * this escrow, booking and provider.
             */
            if (!escrow.getId().equals(existing.getEscrowId())
                    || !bookingId.equals(existing.getBookingId())
                    || !escrow.getProviderId().equals(existing.getProviderId())) {
                throw new IllegalStateException(
                        "Existing labour release does not match escrow "
                                + escrow.getId()
                );
            }

            if (existing.getAmount() == null
                    || existing.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        "Existing labour release has an invalid amount for booking "
                                + bookingId
                );
            }

            if (!escrow.isFullyReleased()) {
                throw new IllegalStateException(
                        "A labour release exists, but escrow is not fully released "
                                + "for booking " + bookingId
                );
            }

            /*
             * Idempotent success:
             * do NOT credit the wallet again;
             * do NOT create another release;
             * do NOT publish another event.
             *
             * The original transaction already performed those operations.
             */
            return EscrowMapper.toResponse(escrow);
        }

        BigDecimal amount = escrow.getRemainingAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "No positive labour balance remains for booking "
                            + bookingId
            );
        }

        /*
         * All following operations participate in this service's
         * transaction. If any operation fails, the escrow update,
         * release record, wallet credit and outbox event must roll back
         * together, assuming the collaborators use the same transaction
         * manager and database.
         */
        escrow.release(amount, EscrowReleaseType.LABOUR);
        escrowRepository.save(escrow);

        EscrowRelease release = escrowReleaseRepository.save(
                escrowReleaseFactory.labourRelease(
                        escrow,
                        amount
                )
        );

        walletService.creditWallet(
                escrow.getProviderId(),
                release.getId(),
                escrow.getId(),
                amount,
                "Labour payment"
        );

        domainEventPublisher.publish(
                escrowEventFactory.labourReleased(
                        escrow,
                        amount
                )
        );

        return EscrowMapper.toResponse(escrow);
    }

    @Override
    public void refundEscrow(UUID bookingId) {
        validateBookingId(bookingId);

        Escrow escrow = getEscrowByBookingIdForUpdate(bookingId);
        escrow.refund();
        escrowRepository.save(escrow);
    }

    private Escrow getEscrowByBookingIdForUpdate(UUID bookingId) {
        return escrowRepository.findByBookingIdForUpdate(bookingId)
                .orElseThrow(
                        () -> new EscrowNotFoundException(
                                bookingId,
                                "booking"
                        )
                );
    }

    private void validateBookingId(UUID bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException(
                    "Booking ID must not be null."
            );
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Materials release amount must be greater than zero."
            );
        }
    }
}
