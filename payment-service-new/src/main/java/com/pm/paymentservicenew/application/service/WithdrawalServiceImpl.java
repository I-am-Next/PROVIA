package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.request.CreateWithdrawalRequest;
import com.pm.paymentservicenew.application.dto.response.WithdrawalResponse;
import com.pm.paymentservicenew.application.event.DomainEventPublisher;
import com.pm.paymentservicenew.application.factory.WithdrawalEventFactory;
import com.pm.paymentservicenew.application.mapper.WithdrawalMapper;
import com.pm.paymentservicenew.domain.exception.WithdrawalNotFoundException;
import com.pm.paymentservicenew.domain.model.withdrawal.Withdrawal;
import com.pm.paymentservicenew.domain.repository.WithdrawalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WithdrawalServiceImpl implements WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;
    private final WalletService walletService;
    private final WithdrawalMapper withdrawalMapper;
    private final DomainEventPublisher domainEventPublisher;
    private final WithdrawalEventFactory withdrawalEventFactory;

    public WithdrawalServiceImpl(
            WithdrawalRepository withdrawalRepository,
            WalletService walletService,
            WithdrawalMapper withdrawalMapper,
            DomainEventPublisher domainEventPublisher,
            WithdrawalEventFactory withdrawalEventFactory
    ) {
        this.withdrawalRepository = withdrawalRepository;
        this.walletService = walletService;
        this.withdrawalMapper = withdrawalMapper;
        this.domainEventPublisher = domainEventPublisher;
        this.withdrawalEventFactory = withdrawalEventFactory;
    }

    @Override
    public WithdrawalResponse requestWithdrawal(
            UUID providerId,
            CreateWithdrawalRequest request
    ) {
        UUID walletId = walletService
                .getWallet(providerId)
                .walletId();

        Withdrawal withdrawal = Withdrawal.create(
                walletId,
                providerId,
                request.amount()
        );

        withdrawalRepository.save(withdrawal);

        walletService.reserveFunds(
                providerId,
                request.amount(),
                withdrawal.getId(),
                "Withdrawal request"
        );

        domainEventPublisher.publish(
                withdrawalEventFactory.withdrawalRequested(withdrawal)
        );

        return withdrawalMapper.toResponse(withdrawal);
    }

    @Override
    public WithdrawalResponse startProcessingWithdrawal(UUID withdrawalId) {
        Withdrawal withdrawal = getWithdrawalOrThrow(withdrawalId);

        // The domain entity enforces PENDING -> PROCESSING.
        withdrawal.startProcessing();

        Withdrawal savedWithdrawal = withdrawalRepository.save(withdrawal);

        return withdrawalMapper.toResponse(savedWithdrawal);
    }

    @Override
    public WithdrawalResponse completeWithdrawal(UUID withdrawalId) {
        Withdrawal withdrawal = getWithdrawalOrThrow(withdrawalId);

        // The domain entity permits completion only from PROCESSING.
        withdrawal.complete();

        walletService.completeWithdrawal(
                withdrawal.getProviderId(),
                withdrawal.getId(),
                withdrawal.getAmount(),
                "Withdrawal completed"
        );

        Withdrawal savedWithdrawal = withdrawalRepository.save(withdrawal);

        domainEventPublisher.publish(
                withdrawalEventFactory.withdrawalCompleted(savedWithdrawal)
        );

        return withdrawalMapper.toResponse(savedWithdrawal);
    }


    @Override
    public void failWithdrawal(
            UUID withdrawalId,
            String reason
    ) {
        Withdrawal withdrawal = getWithdrawalOrThrow(withdrawalId);

        // The domain entity permits failure only from PROCESSING.
        withdrawal.fail(reason);

        walletService.cancelWithdrawal(
                withdrawal.getProviderId(),
                withdrawal.getId(),
                withdrawal.getAmount(),
                "Withdrawal failed: " + reason
        );

        withdrawalRepository.save(withdrawal);

        domainEventPublisher.publish(
                withdrawalEventFactory.withdrawalFailed(withdrawal)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WithdrawalResponse getWithdrawal(
            UUID providerId,
            UUID withdrawalId
    ) {
        Withdrawal withdrawal = withdrawalRepository
                .findByIdAndProviderId(withdrawalId, providerId)
                .orElseThrow(
                        () -> new WithdrawalNotFoundException(
                                withdrawalId,
                                "withdrawal"
                        )
                );

        return withdrawalMapper.toResponse(withdrawal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalResponse> getWithdrawals(UUID providerId) {
        return withdrawalRepository
                .findByProviderId(providerId)
                .stream()
                .map(withdrawalMapper::toResponse)
                .toList();
    }

    private Withdrawal getWithdrawalOrThrow(UUID withdrawalId) {
        return withdrawalRepository.findById(withdrawalId)
                .orElseThrow(
                        () -> new WithdrawalNotFoundException(
                                withdrawalId,
                                "withdrawal"
                        )
                );
    }
}
