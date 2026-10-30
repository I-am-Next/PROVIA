package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.response.EscrowResponse;
import com.pm.paymentservicenew.application.event.DomainEventPublisher;
import com.pm.paymentservicenew.application.factory.EscrowEventFactory;
import com.pm.paymentservicenew.application.factory.EscrowReleaseFactory;
import com.pm.paymentservicenew.domain.exception.EscrowNotFoundException;
import com.pm.paymentservicenew.domain.model.escrow.Escrow;
import com.pm.paymentservicenew.domain.model.escrow.EscrowRelease;
import com.pm.paymentservicenew.domain.model.escrow.EscrowReleaseType;
import com.pm.paymentservicenew.domain.repository.EscrowReleaseRepository;
import com.pm.paymentservicenew.domain.repository.EscrowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EscrowServiceImplTest {

    private static final BigDecimal ESCROW_AMOUNT =
            new BigDecimal("50000.00");

    private static final BigDecimal MATERIALS_AMOUNT =
            new BigDecimal("20000.00");

    private static final BigDecimal LABOUR_AMOUNT =
            new BigDecimal("30000.00");

    @Mock
    private EscrowRepository escrowRepository;

    @Mock
    private EscrowReleaseRepository escrowReleaseRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private EscrowEventFactory escrowEventFactory;

    @Mock
    private EscrowReleaseFactory escrowReleaseFactory;

    private EscrowServiceImpl escrowService;

    private UUID bookingId;
    private UUID quoteId;
    private UUID providerId;
    private UUID customerId;
    private UUID escrowId;

    private Escrow escrow;

    @BeforeEach
    void setUp() {
        escrowService = new EscrowServiceImpl(
                escrowRepository,
                escrowReleaseRepository,
                walletService,
                domainEventPublisher,
                escrowEventFactory,
                escrowReleaseFactory
        );

        UUID paymentId = UUID.randomUUID();

        bookingId = UUID.randomUUID();
        quoteId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        escrowId = UUID.randomUUID();

        escrow = Escrow.create(
                paymentId,
                bookingId,
                customerId,
                providerId,
                ESCROW_AMOUNT
        );

        setEscrowId(escrow, escrowId);
    }

    @Test
    void shouldReleaseMaterialsForQuoteCreditWalletAndPublishEvent() {
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.of(escrow));

        when(escrowReleaseRepository.findByBookingIdAndReleaseType(
                bookingId,
                EscrowReleaseType.MATERIALS
        )).thenReturn(List.of());

        EscrowRelease release = EscrowRelease.create(
                escrowId,
                bookingId,
                quoteId,
                providerId,
                EscrowReleaseType.MATERIALS,
                MATERIALS_AMOUNT,
                "Materials advance"
        );

        when(escrowReleaseFactory.materialsRelease(
                escrow,
                quoteId,
                MATERIALS_AMOUNT
        )).thenReturn(release);

        when(escrowRepository.save(escrow))
                .thenReturn(escrow);

        when(escrowReleaseRepository.save(release))
                .thenReturn(release);

        EscrowResponse response = escrowService.releaseMaterials(
                bookingId,
                quoteId,
                MATERIALS_AMOUNT
        );

        assertNotNull(response);
        assertEquals(
                0,
                MATERIALS_AMOUNT.compareTo(escrow.getReleasedAmount())
        );
        assertTrue(escrow.isPartiallyReleased());

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository).save(escrow);

        verify(escrowReleaseRepository)
                .findByBookingIdAndReleaseType(
                        bookingId,
                        EscrowReleaseType.MATERIALS
                );

        verify(escrowReleaseFactory).materialsRelease(
                escrow,
                quoteId,
                MATERIALS_AMOUNT
        );

        verify(escrowReleaseRepository).save(release);

        verify(walletService).creditWallet(
                providerId,
                release.getId(),
                escrowId,
                MATERIALS_AMOUNT,
                "Materials advance"
        );

        verify(escrowEventFactory).materialsReleased(
                escrow,
                quoteId,
                MATERIALS_AMOUNT
        );

        verify(domainEventPublisher).publish(any());
    }

    @Test
    void shouldReleaseLabourUsingRemainingEscrowAmount() {
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.of(escrow));

        escrow.release(
                MATERIALS_AMOUNT,
                EscrowReleaseType.MATERIALS
        );

        EscrowRelease release = EscrowRelease.create(
                escrowId,
                bookingId,
                null,
                providerId,
                EscrowReleaseType.LABOUR,
                LABOUR_AMOUNT,
                "Labour payment"
        );

        when(escrowReleaseFactory.labourRelease(
                escrow,
                LABOUR_AMOUNT
        )).thenReturn(release);

        when(escrowRepository.save(escrow))
                .thenReturn(escrow);

        when(escrowReleaseRepository.save(release))
                .thenReturn(release);

        escrowService.releaseLabour(bookingId);

        assertEquals(
                0,
                ESCROW_AMOUNT.compareTo(escrow.getReleasedAmount())
        );
        assertTrue(escrow.isFullyReleased());

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository).save(escrow);
        verify(escrowReleaseRepository).save(release);

        verify(walletService).creditWallet(
                providerId,
                release.getId(),
                escrowId,
                LABOUR_AMOUNT,
                "Labour payment"
        );

        verify(escrowEventFactory).labourReleased(
                escrow,
                LABOUR_AMOUNT
        );

        verify(domainEventPublisher).publish(any());
    }

    @Test
    void shouldRefundEscrowAndSaveUpdatedState() {
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.of(escrow));

        when(escrowRepository.save(escrow))
                .thenReturn(escrow);

        escrowService.refundEscrow(bookingId);

        assertTrue(escrow.isRefunded());
        assertNotNull(escrow.getRefundedAt());

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository).save(escrow);
        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldThrowWhenEscrowDoesNotExistForMaterialsRelease() {
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.empty());

        assertThrows(
                EscrowNotFoundException.class,
                () -> escrowService.releaseMaterials(
                        bookingId,
                        quoteId,
                        MATERIALS_AMOUNT
                )
        );

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository, never()).save(any());
        verifyNoInteractions(escrowReleaseRepository);
        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldThrowWhenEscrowDoesNotExistForLabourRelease() {
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.empty());

        assertThrows(
                EscrowNotFoundException.class,
                () -> escrowService.releaseLabour(bookingId)
        );

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository, never()).save(any());
        verifyNoInteractions(escrowReleaseRepository);
        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldThrowWhenEscrowDoesNotExistForRefund() {
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.empty());

        assertThrows(
                EscrowNotFoundException.class,
                () -> escrowService.refundEscrow(bookingId)
        );

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository, never()).save(any());
        verifyNoInteractions(escrowReleaseRepository);
        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldNotReleaseMaterialsWhenAmountExceedsEscrowBalance() {
        BigDecimal excessiveAmount = new BigDecimal("60000.00");

        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.of(escrow));

        assertThrows(
                RuntimeException.class,
                () -> escrowService.releaseMaterials(
                        bookingId,
                        quoteId,
                        excessiveAmount
                )
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(escrow.getReleasedAmount())
        );

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository, never()).save(any());
        verify(escrowReleaseRepository, never()).save(any());
        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldNotReleaseLabourWhenEscrowIsFullyReleased() {
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.of(escrow));

        escrow.release(
                ESCROW_AMOUNT,
                EscrowReleaseType.MATERIALS
        );

        assertTrue(escrow.isFullyReleased());

        assertThrows(
                RuntimeException.class,
                () -> escrowService.releaseLabour(bookingId)
        );

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository, never()).save(any());
        verify(escrowReleaseRepository, never()).save(any());
        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldNotReleaseMaterialsWhenEscrowIsRefunded() {
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.of(escrow));

        escrow.refund();

        assertThrows(
                RuntimeException.class,
                () -> escrowService.releaseMaterials(
                        bookingId,
                        quoteId,
                        new BigDecimal("10000.00")
                )
        );

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository, never()).save(any());
        verify(escrowReleaseRepository, never()).save(any());
        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldNotRefundFullyReleasedEscrow() {
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.of(escrow));

        escrow.release(
                ESCROW_AMOUNT,
                EscrowReleaseType.MATERIALS
        );

        assertTrue(escrow.isFullyReleased());

        assertThrows(
                RuntimeException.class,
                () -> escrowService.refundEscrow(bookingId)
        );

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository, never()).save(any());
        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldNotRefundAlreadyRefundedEscrow() {
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.of(escrow));

        escrow.refund();

        assertThrows(
                RuntimeException.class,
                () -> escrowService.refundEscrow(bookingId)
        );

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);
        verify(escrowRepository, never()).save(any());
        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
    }


    @Test
    void shouldNotReleaseMaterialsWhenAmountIsZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> escrowService.releaseMaterials(
                        bookingId,
                        quoteId,
                        BigDecimal.ZERO
                )
        );

        verifyNoInteractions(escrowRepository);
        verifyNoInteractions(escrowReleaseRepository);
        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
        verifyNoInteractions(escrowEventFactory);
        verifyNoInteractions(escrowReleaseFactory);
    }

    private void setEscrowId(Escrow target, UUID id) {
        try {
            var field = Escrow.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Unable to set escrow ID for test.",
                    ex
            );
        }
    }

    @Test
    void shouldReturnExistingEscrowWhenLabourWasAlreadyReleased() {
        // Arrange: the escrow has already been fully released.
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.of(escrow));

        escrow.release(
                MATERIALS_AMOUNT,
                EscrowReleaseType.MATERIALS
        );
        escrow.release(
                LABOUR_AMOUNT,
                EscrowReleaseType.LABOUR
        );

        assertTrue(escrow.isFullyReleased());

        EscrowRelease existingLabourRelease = EscrowRelease.create(
                escrowId,
                bookingId,
                null,
                providerId,
                EscrowReleaseType.LABOUR,
                LABOUR_AMOUNT,
                "Labour payment"
        );

        when(escrowReleaseRepository.findByBookingIdAndReleaseType(
                bookingId,
                EscrowReleaseType.LABOUR
        )).thenReturn(List.of(existingLabourRelease));

        // Act
        EscrowResponse response = escrowService.releaseLabour(bookingId);

        // Assert
        assertNotNull(response);

        verify(escrowRepository).findByBookingIdForUpdate(bookingId);

        verify(escrowReleaseRepository).findByBookingIdAndReleaseType(
                bookingId,
                EscrowReleaseType.LABOUR
        );

        verify(escrowRepository, never()).save(any());
        verify(escrowReleaseRepository, never()).save(any());

        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
        verifyNoInteractions(escrowEventFactory);
        verifyNoInteractions(escrowReleaseFactory);
    }


    @Test
    void shouldThrowWhenMultipleLabourReleasesAlreadyExist() {
        // Arrange
        when(escrowRepository.findByBookingIdForUpdate(bookingId))
                .thenReturn(Optional.of(escrow));

        escrow.release(
                MATERIALS_AMOUNT,
                EscrowReleaseType.MATERIALS
        );
        escrow.release(
                LABOUR_AMOUNT,
                EscrowReleaseType.LABOUR
        );

        EscrowRelease firstRelease = EscrowRelease.create(
                escrowId,
                bookingId,
                null,
                providerId,
                EscrowReleaseType.LABOUR,
                LABOUR_AMOUNT,
                "Labour payment"
        );

        EscrowRelease secondRelease = EscrowRelease.create(
                escrowId,
                bookingId,
                null,
                providerId,
                EscrowReleaseType.LABOUR,
                LABOUR_AMOUNT,
                "Duplicate labour payment"
        );

        when(escrowReleaseRepository.findByBookingIdAndReleaseType(
                bookingId,
                EscrowReleaseType.LABOUR
        )).thenReturn(List.of(firstRelease, secondRelease));

        // Act and assert
        assertThrows(
                IllegalStateException.class,
                () -> escrowService.releaseLabour(bookingId)
        );

        // Verify no additional financial operations occur.
        verify(escrowRepository).findByBookingIdForUpdate(bookingId);

        verify(escrowReleaseRepository).findByBookingIdAndReleaseType(
                bookingId,
                EscrowReleaseType.LABOUR
        );

        verify(escrowRepository, never()).save(any());
        verify(escrowReleaseRepository, never()).save(any());

        verifyNoInteractions(walletService);
        verifyNoInteractions(domainEventPublisher);
        verifyNoInteractions(escrowEventFactory);
        verifyNoInteractions(escrowReleaseFactory);
    }



}
