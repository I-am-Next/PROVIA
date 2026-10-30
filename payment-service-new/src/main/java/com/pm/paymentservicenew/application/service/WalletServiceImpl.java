package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.response.WalletResponse;
import com.pm.paymentservicenew.application.dto.response.WalletTransactionResponse;
import com.pm.paymentservicenew.application.event.DomainEventPublisher;
import com.pm.paymentservicenew.application.factory.WalletEventFactory;
import com.pm.paymentservicenew.application.mapper.WalletMapper;
import com.pm.paymentservicenew.application.mapper.WalletTransactionMapper;
import com.pm.paymentservicenew.domain.model.wallet.Wallet;
import com.pm.paymentservicenew.domain.model.wallet.WalletTransaction;
import com.pm.paymentservicenew.domain.model.wallet.WalletTransactionType;
import com.pm.paymentservicenew.domain.repository.WalletRepository;
import com.pm.paymentservicenew.domain.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final DomainEventPublisher domainEventPublisher;
    private final WalletEventFactory walletEventFactory;

    public WalletServiceImpl(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            WalletMapper walletMapper,
            WalletTransactionMapper walletTransactionMapper,
            DomainEventPublisher domainEventPublisher,
            WalletEventFactory walletEventFactory
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletMapper = walletMapper;
        this.walletTransactionMapper = walletTransactionMapper;
        this.domainEventPublisher = domainEventPublisher;
        this.walletEventFactory = walletEventFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID providerId) {
        Objects.requireNonNull(providerId, "providerId must not be null");

        return walletMapper.toResponse(getOrCreateWallet(providerId));
    }

    @Override
    public void creditWallet(
            UUID providerId,
            UUID operationId,
            UUID escrowId,
            BigDecimal amount,
            String description
    ) {
        validateOperation(providerId, operationId, amount);
        Objects.requireNonNull(escrowId, "escrowId must not be null");

        Wallet wallet = getWalletForUpdate(providerId);

        if (isExistingOperation(
                wallet,
                providerId,
                operationId,
                WalletTransactionType.CREDIT,
                amount
        )) {
            return;
        }

        wallet.credit(amount);
        walletRepository.save(wallet);

        saveTransaction(
                wallet,
                providerId,
                WalletTransactionType.CREDIT,
                amount,
                operationId,
                description
        );

        domainEventPublisher.publish(
                walletEventFactory.walletCredited(
                        wallet,
                        operationId,
                        escrowId,
                        amount
                )
        );
    }

    @Override
    public void reserveFunds(
            UUID providerId,
            BigDecimal amount,
            UUID withdrawalId,
            String description
    ) {
        validateOperation(providerId, withdrawalId, amount);

        Wallet wallet = getWalletForUpdate(providerId);

        if (isExistingOperation(
                wallet,
                providerId,
                withdrawalId,
                WalletTransactionType.RESERVE,
                amount
        )) {
            return;
        }

        wallet.reserve(amount);
        walletRepository.save(wallet);

        saveTransaction(
                wallet,
                providerId,
                WalletTransactionType.RESERVE,
                amount,
                withdrawalId,
                description
        );

        domainEventPublisher.publish(
                walletEventFactory.walletFundsReserved(
                        wallet,
                        withdrawalId,
                        amount
                )
        );
    }

    @Override
    public void completeWithdrawal(
            UUID providerId,
            UUID withdrawalId,
            BigDecimal amount,
            String description
    ) {
        validateOperation(providerId, withdrawalId, amount);

        Wallet wallet = getWalletForUpdate(providerId);

        if (isExistingOperation(
                wallet,
                providerId,
                withdrawalId,
                WalletTransactionType.WITHDRAWAL,
                amount
        )) {
            return;
        }

        requireMatchingReservation(
                wallet,
                providerId,
                withdrawalId,
                amount
        );

        wallet.withdrawReserved(amount);
        walletRepository.save(wallet);

        saveTransaction(
                wallet,
                providerId,
                WalletTransactionType.WITHDRAWAL,
                amount,
                withdrawalId,
                description
        );

        domainEventPublisher.publish(
                walletEventFactory.walletWithdrawalCompleted(
                        wallet,
                        withdrawalId,
                        amount
                )
        );
    }

    @Override
    public void cancelWithdrawal(
            UUID providerId,
            UUID withdrawalId,
            BigDecimal amount,
            String description
    ) {
        validateOperation(providerId, withdrawalId, amount);

        Wallet wallet = getWalletForUpdate(providerId);

        if (isExistingOperation(
                wallet,
                providerId,
                withdrawalId,
                WalletTransactionType.RESERVE_RELEASE,
                amount
        )) {
            return;
        }

        requireMatchingReservation(
                wallet,
                providerId,
                withdrawalId,
                amount
        );

        wallet.releaseReserved(amount);
        walletRepository.save(wallet);

        saveTransaction(
                wallet,
                providerId,
                WalletTransactionType.RESERVE_RELEASE,
                amount,
                withdrawalId,
                description
        );

        domainEventPublisher.publish(
                walletEventFactory.walletWithdrawalCancelled(
                        wallet,
                        withdrawalId,
                        amount
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactions(UUID providerId) {
        Objects.requireNonNull(providerId, "providerId must not be null");

        Wallet wallet = getOrCreateWallet(providerId);

        return walletTransactionRepository
                .findByWalletIdOrderByOccurredAtDesc(wallet.getId())
                .stream()
                .map(walletTransactionMapper::toResponse)
                .toList();
    }

    private boolean isExistingOperation(
            Wallet wallet,
            UUID providerId,
            UUID referenceId,
            WalletTransactionType type,
            BigDecimal amount
    ) {
        return walletTransactionRepository
                .findByReferenceIdAndType(referenceId, type)
                .map(existing -> {
                    boolean sameOperation =
                            existing.getProviderId().equals(providerId)
                                    && existing.getWalletId().equals(wallet.getId())
                                    && existing.getAmount().compareTo(amount) == 0;

                    if (!sameOperation) {
                        throw new IllegalStateException(
                                "Wallet operation ID was reused with "
                                        + "different operation details. "
                                        + "Reference: " + referenceId
                                        + ", type: " + type
                        );
                    }

                    return true;
                })
                .orElse(false);
    }

    private void requireMatchingReservation(
            Wallet wallet,
            UUID providerId,
            UUID withdrawalId,
            BigDecimal amount
    ) {
        boolean reservationExists =
                walletTransactionRepository
                        .findByReferenceIdAndType(
                                withdrawalId,
                                WalletTransactionType.RESERVE
                        )
                        .map(reservation -> {
                            boolean matches =
                                    reservation.getProviderId().equals(providerId)
                                            && reservation.getWalletId().equals(wallet.getId())
                                            && reservation.getAmount().compareTo(amount) == 0;

                            if (!matches) {
                                throw new IllegalStateException(
                                        "Withdrawal reservation details do not match "
                                                + "the requested wallet operation: "
                                                + withdrawalId
                                );
                            }

                            return true;
                        })
                        .orElse(false);

        if (!reservationExists) {
            throw new IllegalStateException(
                    "Cannot complete or release a withdrawal without "
                            + "a matching reservation: " + withdrawalId
            );
        }
    }

    private void saveTransaction(
            Wallet wallet,
            UUID providerId,
            WalletTransactionType type,
            BigDecimal amount,
            UUID referenceId,
            String description
    ) {
        walletTransactionRepository.save(
                WalletTransaction.create(
                        wallet.getId(),
                        providerId,
                        type,
                        amount,
                        referenceId,
                        description
                )
        );
    }

    private Wallet getWalletForUpdate(UUID providerId) {
        return walletRepository.findByProviderIdForUpdate(providerId)
                .orElseGet(() -> getOrCreateWallet(providerId));
    }

    private Wallet getOrCreateWallet(UUID providerId) {
        return walletRepository.findByProviderId(providerId)
                .orElseGet(() -> walletRepository.save(
                        Wallet.create(providerId)
                ));
    }

    private void validateOperation(
            UUID providerId,
            UUID operationId,
            BigDecimal amount
    ) {
        Objects.requireNonNull(providerId, "providerId must not be null");
        Objects.requireNonNull(operationId, "operationId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Wallet operation amount must be greater than zero."
            );
        }
    }
}
