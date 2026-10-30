
package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.response.WalletResponse;
import com.pm.paymentservicenew.application.dto.response.WalletTransactionResponse;
import com.pm.paymentservicenew.application.event.DomainEventPublisher;
import com.pm.paymentservicenew.application.factory.WalletEventFactory;
import com.pm.paymentservicenew.application.mapper.WalletMapper;
import com.pm.paymentservicenew.application.mapper.WalletTransactionMapper;
import com.pm.paymentservicenew.domain.exception.InsufficientWalletBalanceException;
import com.pm.paymentservicenew.domain.model.wallet.Wallet;
import com.pm.paymentservicenew.domain.model.wallet.WalletTransaction;
import com.pm.paymentservicenew.domain.model.wallet.WalletTransactionType;
import com.pm.paymentservicenew.domain.repository.WalletRepository;
import com.pm.paymentservicenew.domain.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private WalletTransactionMapper walletTransactionMapper;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private WalletEventFactory walletEventFactory;

    private WalletServiceImpl walletService;

    private UUID providerId;
    private UUID walletId;
    private UUID referenceId;
    private UUID escrowId;

    @BeforeEach
    void setUp() {
        walletService = new WalletServiceImpl(
                walletRepository,
                walletTransactionRepository,
                walletMapper,
                walletTransactionMapper,
                domainEventPublisher,
                walletEventFactory
        );

        providerId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        referenceId = UUID.randomUUID();
        escrowId = UUID.randomUUID();
    }

    @Test
    void shouldReturnExistingWallet() {
        Wallet wallet = createWallet();

        WalletResponse response = new WalletResponse(
                walletId,
                providerId,
                new BigDecimal("100000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100000.00")
        );

        when(walletRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(wallet));
        when(walletMapper.toResponse(wallet)).thenReturn(response);

        assertSame(response, walletService.getWallet(providerId));

        verify(walletRepository).findByProviderId(providerId);
        verify(walletMapper).toResponse(wallet);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void shouldCreateWalletWhenWalletDoesNotExist() {
        Wallet wallet = createWallet();

        WalletResponse response = new WalletResponse(
                walletId,
                providerId,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(walletRepository.findByProviderId(providerId))
                .thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletMapper.toResponse(wallet)).thenReturn(response);

        assertSame(response, walletService.getWallet(providerId));

        ArgumentCaptor<Wallet> captor =
                ArgumentCaptor.forClass(Wallet.class);

        verify(walletRepository).save(captor.capture());

        Wallet savedWallet = captor.getValue();

        assertEquals(providerId, savedWallet.getProviderId());
        assertEquals(BigDecimal.ZERO, savedWallet.getAvailableBalance());
        assertEquals(BigDecimal.ZERO, savedWallet.getReservedBalance());
        assertEquals(BigDecimal.ZERO, savedWallet.getPendingBalance());

        verify(walletMapper).toResponse(wallet);
    }

    @Test
    void shouldCreditWalletAndCreateCreditTransaction() {
        Wallet wallet = createWallet();
        BigDecimal amount = new BigDecimal("50000.00");

        stubLockedWallet(wallet);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.CREDIT
        )).thenReturn(Optional.empty());
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.creditWallet(
                providerId, referenceId, escrowId, amount, "Escrow release"
        );

        assertMoneyEquals(amount, wallet.getAvailableBalance());
        assertMoneyEquals(BigDecimal.ZERO, wallet.getReservedBalance());
        assertMoneyEquals(BigDecimal.ZERO, wallet.getPendingBalance());

        ArgumentCaptor<WalletTransaction> captor =
                ArgumentCaptor.forClass(WalletTransaction.class);

        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(captor.capture());

        WalletTransaction transaction = captor.getValue();

        assertEquals(walletId, transaction.getWalletId());
        assertEquals(providerId, transaction.getProviderId());
        assertEquals(WalletTransactionType.CREDIT, transaction.getType());
        assertMoneyEquals(amount, transaction.getAmount());
        assertEquals(referenceId, transaction.getReferenceId());
        assertEquals("Escrow release", transaction.getDescription());

        verify(domainEventPublisher).publish(any());
    }

    @Test
    void shouldPublishWalletCreditedEventUsingOperationId() {
        Wallet wallet = createWallet();
        BigDecimal amount = new BigDecimal("50000.00");

        stubLockedWallet(wallet);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.CREDIT
        )).thenReturn(Optional.empty());
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.creditWallet(
                providerId, referenceId, escrowId, amount, "Escrow release"
        );

        verify(walletEventFactory).walletCredited(
                wallet,
                referenceId,
                escrowId,
                amount
        );

        verify(domainEventPublisher).publish(any());
    }

    @Test
    void shouldRejectInvalidCreditAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.creditWallet(
                        providerId,
                        referenceId,
                        escrowId,
                        BigDecimal.ZERO,
                        "Invalid credit"
                )
        );

        verifyNoInteractions(
                walletRepository,
                walletTransactionRepository,
                walletMapper,
                walletTransactionMapper,
                domainEventPublisher,
                walletEventFactory
        );
    }

    @Test
    void shouldReserveAvailableFunds() {
        Wallet wallet = createWallet();
        wallet.credit(new BigDecimal("100000.00"));
        BigDecimal amount = new BigDecimal("40000.00");

        stubLockedWallet(wallet);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.RESERVE
        )).thenReturn(Optional.empty());
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.reserveFunds(
                providerId, amount, referenceId, "Withdrawal request"
        );

        assertMoneyEquals(new BigDecimal("60000.00"),
                wallet.getAvailableBalance());
        assertMoneyEquals(new BigDecimal("40000.00"),
                wallet.getReservedBalance());

        ArgumentCaptor<WalletTransaction> captor =
                ArgumentCaptor.forClass(WalletTransaction.class);

        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(captor.capture());

        WalletTransaction transaction = captor.getValue();

        assertEquals(WalletTransactionType.RESERVE, transaction.getType());
        assertMoneyEquals(amount, transaction.getAmount());
        assertEquals(referenceId, transaction.getReferenceId());

        verify(domainEventPublisher).publish(any());
    }

    @Test
    void shouldPublishFundsReservedEvent() {
        Wallet wallet = createWallet();
        wallet.credit(new BigDecimal("100000.00"));
        BigDecimal amount = new BigDecimal("40000.00");

        stubLockedWallet(wallet);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.RESERVE
        )).thenReturn(Optional.empty());
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.reserveFunds(
                providerId, amount, referenceId, "Withdrawal request"
        );

        verify(walletEventFactory)
                .walletFundsReserved(wallet, referenceId, amount);
        verify(domainEventPublisher).publish(any());
    }

    @Test
    void shouldRejectReservationWhenAvailableBalanceIsInsufficient() {
        Wallet wallet = createWallet();
        wallet.credit(new BigDecimal("30000.00"));

        stubLockedWallet(wallet);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.RESERVE
        )).thenReturn(Optional.empty());

        assertThrows(
                InsufficientWalletBalanceException.class,
                () -> walletService.reserveFunds(
                        providerId,
                        new BigDecimal("50000.00"),
                        referenceId,
                        "Withdrawal request"
                )
        );

        verify(walletRepository, never()).save(any());
        verify(walletTransactionRepository, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void shouldRejectInvalidReservationAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.reserveFunds(
                        providerId,
                        BigDecimal.ZERO,
                        referenceId,
                        "Invalid withdrawal"
                )
        );

        verifyNoInteractions(
                walletRepository,
                walletTransactionRepository,
                domainEventPublisher,
                walletEventFactory
        );
    }

    @Test
    void shouldCompleteWithdrawalAndConsumeReservedFunds() {
        Wallet wallet = createWallet();
        BigDecimal amount = new BigDecimal("50000.00");

        wallet.credit(new BigDecimal("100000.00"));
        wallet.reserve(amount);

        stubLockedWallet(wallet);
        stubReservation(wallet, amount);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.WITHDRAWAL
        )).thenReturn(Optional.empty());
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.completeWithdrawal(
                providerId, referenceId, amount, "Withdrawal completed"
        );

        assertMoneyEquals(new BigDecimal("50000.00"),
                wallet.getAvailableBalance());
        assertMoneyEquals(BigDecimal.ZERO, wallet.getReservedBalance());

        ArgumentCaptor<WalletTransaction> captor =
                ArgumentCaptor.forClass(WalletTransaction.class);

        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(captor.capture());

        WalletTransaction transaction = captor.getValue();

        assertEquals(WalletTransactionType.WITHDRAWAL, transaction.getType());
        assertMoneyEquals(amount, transaction.getAmount());
        assertEquals(referenceId, transaction.getReferenceId());

        verify(domainEventPublisher).publish(any());
    }

    @Test
    void shouldPublishWithdrawalCompletedEvent() {
        Wallet wallet = createWallet();
        BigDecimal amount = new BigDecimal("50000.00");

        wallet.credit(new BigDecimal("100000.00"));
        wallet.reserve(amount);

        stubLockedWallet(wallet);
        stubReservation(wallet, amount);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.WITHDRAWAL
        )).thenReturn(Optional.empty());
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.completeWithdrawal(
                providerId, referenceId, amount, "Withdrawal completed"
        );

        verify(walletEventFactory)
                .walletWithdrawalCompleted(wallet, referenceId, amount);
        verify(domainEventPublisher).publish(any());
    }

    @Test
    void shouldRejectWithdrawalWhenReservedBalanceIsInsufficient() {
        Wallet wallet = createWallet();
        BigDecimal attemptedAmount = new BigDecimal("50000.00");

        wallet.credit(new BigDecimal("100000.00"));
        wallet.reserve(new BigDecimal("30000.00"));

        stubLockedWallet(wallet);
        stubReservation(wallet, attemptedAmount);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.WITHDRAWAL
        )).thenReturn(Optional.empty());

        assertThrows(
                InsufficientWalletBalanceException.class,
                () -> walletService.completeWithdrawal(
                        providerId,
                        referenceId,
                        attemptedAmount,
                        "Withdrawal completed"
                )
        );

        verify(walletRepository, never()).save(any());
        verify(walletTransactionRepository, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void shouldRejectInvalidWithdrawalAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.completeWithdrawal(
                        providerId,
                        referenceId,
                        BigDecimal.ZERO,
                        "Invalid withdrawal"
                )
        );

        verifyNoInteractions(
                walletRepository,
                walletTransactionRepository,
                domainEventPublisher,
                walletEventFactory
        );
    }

    @Test
    void shouldCancelWithdrawalAndReleaseReservedFunds() {
        Wallet wallet = createWallet();
        BigDecimal amount = new BigDecimal("50000.00");

        wallet.credit(new BigDecimal("100000.00"));
        wallet.reserve(amount);

        stubLockedWallet(wallet);
        stubReservation(wallet, amount);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.RESERVE_RELEASE
        )).thenReturn(Optional.empty());
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.cancelWithdrawal(
                providerId, referenceId, amount, "Withdrawal failed"
        );

        assertMoneyEquals(new BigDecimal("100000.00"),
                wallet.getAvailableBalance());
        assertMoneyEquals(BigDecimal.ZERO, wallet.getReservedBalance());

        ArgumentCaptor<WalletTransaction> captor =
                ArgumentCaptor.forClass(WalletTransaction.class);

        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(captor.capture());

        WalletTransaction transaction = captor.getValue();

        assertEquals(WalletTransactionType.RESERVE_RELEASE,
                transaction.getType());
        assertMoneyEquals(amount, transaction.getAmount());
        assertEquals(referenceId, transaction.getReferenceId());
        assertEquals("Withdrawal failed", transaction.getDescription());

        verify(domainEventPublisher).publish(any());
    }

    @Test
    void shouldPublishWithdrawalCancelledEvent() {
        Wallet wallet = createWallet();
        BigDecimal amount = new BigDecimal("50000.00");

        wallet.credit(new BigDecimal("100000.00"));
        wallet.reserve(amount);

        stubLockedWallet(wallet);
        stubReservation(wallet, amount);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.RESERVE_RELEASE
        )).thenReturn(Optional.empty());
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.cancelWithdrawal(
                providerId, referenceId, amount, "Withdrawal failed"
        );

        verify(walletEventFactory)
                .walletWithdrawalCancelled(wallet, referenceId, amount);
        verify(domainEventPublisher).publish(any());
    }

    @Test
    void shouldRejectCancellationWhenReservedBalanceIsInsufficient() {
        Wallet wallet = createWallet();
        BigDecimal attemptedAmount = new BigDecimal("50000.00");

        wallet.credit(new BigDecimal("100000.00"));
        wallet.reserve(new BigDecimal("30000.00"));

        stubLockedWallet(wallet);
        stubReservation(wallet, attemptedAmount);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.RESERVE_RELEASE
        )).thenReturn(Optional.empty());

        assertThrows(
                InsufficientWalletBalanceException.class,
                () -> walletService.cancelWithdrawal(
                        providerId,
                        referenceId,
                        attemptedAmount,
                        "Withdrawal failed"
                )
        );

        verify(walletRepository, never()).save(any());
        verify(walletTransactionRepository, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void shouldRejectInvalidCancellationAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.cancelWithdrawal(
                        providerId,
                        referenceId,
                        BigDecimal.ZERO,
                        "Invalid cancellation"
                )
        );

        verifyNoInteractions(
                walletRepository,
                walletTransactionRepository,
                domainEventPublisher,
                walletEventFactory
        );
    }

    @Test
    void shouldReturnWalletTransactions() {
        Wallet wallet = createWallet();

        WalletTransaction transaction = WalletTransaction.create(
                wallet.getId(),
                providerId,
                WalletTransactionType.CREDIT,
                new BigDecimal("50000.00"),
                referenceId,
                "Escrow release"
        );

        WalletTransactionResponse response =
                new WalletTransactionResponse(
                        UUID.randomUUID(),
                        WalletTransactionType.CREDIT,
                        new BigDecimal("50000.00"),
                        referenceId,
                        "Escrow release",
                        Instant.now()
                );

        when(walletRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(wallet));
        when(walletTransactionRepository
                .findByWalletIdOrderByOccurredAtDesc(wallet.getId()))
                .thenReturn(List.of(transaction));
        when(walletTransactionMapper.toResponse(transaction))
                .thenReturn(response);

        List<WalletTransactionResponse> result =
                walletService.getTransactions(providerId);

        assertEquals(1, result.size());
        assertSame(response, result.getFirst());

        verify(walletTransactionRepository)
                .findByWalletIdOrderByOccurredAtDesc(wallet.getId());
        verify(walletTransactionMapper).toResponse(transaction);
    }

    @Test
    void shouldReturnEmptyTransactionListWhenWalletHasNoTransactions() {
        Wallet wallet = createWallet();

        when(walletRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(wallet));
        when(walletTransactionRepository
                .findByWalletIdOrderByOccurredAtDesc(wallet.getId()))
                .thenReturn(List.of());

        List<WalletTransactionResponse> result =
                walletService.getTransactions(providerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(walletTransactionRepository)
                .findByWalletIdOrderByOccurredAtDesc(wallet.getId());
        verifyNoInteractions(walletTransactionMapper);
    }

    @Test
    void shouldCreateWalletWhenGettingTransactionsForProviderWithoutWallet() {
        Wallet wallet = createWallet();

        when(walletRepository.findByProviderId(providerId))
                .thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletTransactionRepository
                .findByWalletIdOrderByOccurredAtDesc(wallet.getId()))
                .thenReturn(List.of());

        List<WalletTransactionResponse> result =
                walletService.getTransactions(providerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(walletRepository).save(any(Wallet.class));
        verify(walletTransactionRepository)
                .findByWalletIdOrderByOccurredAtDesc(wallet.getId());
    }

    @Test
    void shouldNotCreditWalletTwiceForSameOperation() {
        Wallet wallet = createWallet();
        BigDecimal amount = new BigDecimal("50000.00");

        WalletTransaction existing = WalletTransaction.create(
                walletId,
                providerId,
                WalletTransactionType.CREDIT,
                amount,
                referenceId,
                "Original credit"
        );

        stubLockedWallet(wallet);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.CREDIT
        )).thenReturn(Optional.of(existing));

        walletService.creditWallet(
                providerId, referenceId, escrowId, amount, "Duplicate credit"
        );

        assertMoneyEquals(BigDecimal.ZERO, wallet.getAvailableBalance());
        verify(walletRepository, never()).save(any());
        verify(walletTransactionRepository, never()).save(any());
        verifyNoInteractions(domainEventPublisher, walletEventFactory);
    }

    @Test
    void shouldNotReserveFundsTwiceForSameWithdrawal() {
        Wallet wallet = createWallet();
        BigDecimal amount = new BigDecimal("40000.00");

        WalletTransaction existing = WalletTransaction.create(
                walletId,
                providerId,
                WalletTransactionType.RESERVE,
                amount,
                referenceId,
                "Original reservation"
        );

        stubLockedWallet(wallet);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.RESERVE
        )).thenReturn(Optional.of(existing));

        walletService.reserveFunds(
                providerId, amount, referenceId, "Duplicate reservation"
        );

        assertMoneyEquals(BigDecimal.ZERO, wallet.getAvailableBalance());
        assertMoneyEquals(BigDecimal.ZERO, wallet.getReservedBalance());

        verify(walletRepository, never()).save(any());
        verify(walletTransactionRepository, never()).save(any());
        verifyNoInteractions(domainEventPublisher, walletEventFactory);
    }

    @Test
    void shouldRejectReusingOperationIdWithDifferentAmount() {
        Wallet wallet = createWallet();

        WalletTransaction existing = WalletTransaction.create(
                walletId,
                providerId,
                WalletTransactionType.CREDIT,
                new BigDecimal("10000.00"),
                referenceId,
                "Original credit"
        );

        stubLockedWallet(wallet);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.CREDIT
        )).thenReturn(Optional.of(existing));

        assertThrows(
                IllegalStateException.class,
                () -> walletService.creditWallet(
                        providerId,
                        referenceId,
                        escrowId,
                        new BigDecimal("20000.00"),
                        "Different amount"
                )
        );

        verify(walletRepository, never()).save(any());
        verify(walletTransactionRepository, never()).save(any());
        verifyNoInteractions(domainEventPublisher, walletEventFactory);
    }

    @Test
    void shouldRejectCompletionWithoutReservationLedgerEntry() {
        Wallet wallet = createWallet();
        BigDecimal amount = new BigDecimal("10000.00");

        wallet.credit(amount);
        wallet.reserve(amount);

        stubLockedWallet(wallet);
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.WITHDRAWAL
        )).thenReturn(Optional.empty());
        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId, WalletTransactionType.RESERVE
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> walletService.completeWithdrawal(
                        providerId, referenceId, amount, "Complete"
                )
        );

        verify(walletRepository, never()).save(any());
        verify(walletTransactionRepository, never()).save(any());
        verifyNoInteractions(domainEventPublisher, walletEventFactory);
    }

    private void stubLockedWallet(Wallet wallet) {
        when(walletRepository.findByProviderIdForUpdate(providerId))
                .thenReturn(Optional.of(wallet));
    }

    private void stubReservation(Wallet wallet, BigDecimal amount) {
        WalletTransaction reservation = WalletTransaction.create(
                wallet.getId(),
                providerId,
                WalletTransactionType.RESERVE,
                amount,
                referenceId,
                "Withdrawal request"
        );

        when(walletTransactionRepository.findByReferenceIdAndType(
                referenceId,
                WalletTransactionType.RESERVE
        )).thenReturn(Optional.of(reservation));
    }

    private void assertMoneyEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(
                0,
                expected.compareTo(actual),
                () -> "Expected " + expected + " but was " + actual
        );
    }

    private Wallet createWallet() {
        Wallet wallet = Wallet.create(providerId);
        setWalletId(wallet, walletId);
        return wallet;
    }

    private void setWalletId(Wallet wallet, UUID id) {
        try {
            var field = Wallet.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(wallet, id);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(
                    "Could not set wallet id for test",
                    ex
            );
        }
    }
}
