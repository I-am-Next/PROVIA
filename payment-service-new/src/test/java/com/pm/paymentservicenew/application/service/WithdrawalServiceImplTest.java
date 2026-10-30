package com.pm.paymentservicenew.application.service;

import com.pm.contracts.wallet.WithdrawalCompletedEvent;
import com.pm.contracts.wallet.WithdrawalFailedEvent;
import com.pm.paymentservicenew.application.event.DomainEventPublisher;
import com.pm.paymentservicenew.application.factory.WithdrawalEventFactory;
import com.pm.paymentservicenew.application.mapper.WithdrawalMapper;
import com.pm.paymentservicenew.domain.exception.WithdrawalNotFoundException;
import com.pm.paymentservicenew.domain.model.withdrawal.Withdrawal;
import com.pm.paymentservicenew.domain.model.withdrawal.WithdrawalStatus;
import com.pm.paymentservicenew.domain.repository.WithdrawalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceImplTest {

    @Mock
    private WithdrawalRepository withdrawalRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private WithdrawalMapper withdrawalMapper;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private WithdrawalEventFactory withdrawalEventFactory;

    @Mock
    private WithdrawalCompletedEvent withdrawalCompletedEvent;

    @Mock
    private WithdrawalFailedEvent withdrawalFailedEvent;

    private WithdrawalServiceImpl withdrawalService;

    private UUID withdrawalId;
    private UUID providerId;
    private UUID walletId;

    private BigDecimal amount;

    @BeforeEach
    void setUp() {

        withdrawalService = new WithdrawalServiceImpl(
                withdrawalRepository,
                walletService,
                withdrawalMapper,
                domainEventPublisher,
                withdrawalEventFactory
        );

        withdrawalId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        walletId = UUID.randomUUID();

        amount = new BigDecimal("50000.00");
    }

    @Test
    void shouldCompleteWithdrawalAndConsumeReservedFunds() {

        Withdrawal withdrawal =
                createProcessingWithdrawal();

        when(withdrawalRepository.findById(withdrawalId))
                .thenReturn(Optional.of(withdrawal));

        when(withdrawalEventFactory.withdrawalCompleted(withdrawal))
                .thenReturn(withdrawalCompletedEvent);

        withdrawalService.completeWithdrawal(
                withdrawalId
        );

        assertEquals(
                WithdrawalStatus.COMPLETED,
                withdrawal.getStatus()
        );

        verify(walletService)
                .completeWithdrawal(
                        providerId,
                        withdrawal.getId(),
                        amount,
                        "Withdrawal completed"
                );

        verify(withdrawalRepository)
                .save(withdrawal);

        verify(withdrawalEventFactory)
                .withdrawalCompleted(withdrawal);

        verify(domainEventPublisher)
                .publish(withdrawalCompletedEvent);
    }

    @Test
    void shouldFailWithdrawalAndReleaseReservedFunds() {

        Withdrawal withdrawal =
                createProcessingWithdrawal();

        String reason =
                "Bank transfer failed";

        when(withdrawalRepository.findById(withdrawalId))
                .thenReturn(Optional.of(withdrawal));

        when(withdrawalEventFactory.withdrawalFailed(withdrawal))
                .thenReturn(withdrawalFailedEvent);

        withdrawalService.failWithdrawal(
                withdrawalId,
                reason
        );

        assertEquals(
                WithdrawalStatus.FAILED,
                withdrawal.getStatus()
        );

        verify(walletService)
                .cancelWithdrawal(
                        providerId,
                        withdrawal.getId(),
                        amount,
                        "Withdrawal failed: " + reason
                );
        verify(withdrawalRepository)
                .save(withdrawal);

        verify(withdrawalEventFactory)
                .withdrawalFailed(withdrawal);

        verify(domainEventPublisher)
                .publish(withdrawalFailedEvent);
    }

    @Test
    void shouldThrowWhenCompletingNonExistentWithdrawal() {

        when(withdrawalRepository.findById(withdrawalId))
                .thenReturn(Optional.empty());

        assertThrows(
                WithdrawalNotFoundException.class,
                () -> withdrawalService.completeWithdrawal(
                        withdrawalId
                )
        );

        verify(walletService, never())
                .completeWithdrawal(
                        any(),
                        any(),
                        any(),
                        anyString()
                );

        verify(withdrawalRepository, never())
                .save(any());

        verifyNoInteractions(
                domainEventPublisher
        );
    }

    @Test
    void shouldThrowWhenFailingNonExistentWithdrawal() {

        String reason =
                "Bank transfer failed";

        when(withdrawalRepository.findById(withdrawalId))
                .thenReturn(Optional.empty());

        assertThrows(
                WithdrawalNotFoundException.class,
                () -> withdrawalService.failWithdrawal(
                        withdrawalId,
                        reason
                )
        );

        verify(walletService, never())
                .cancelWithdrawal(
                        any(),
                        any(),
                        any(),
                        anyString()
                );

        verify(withdrawalRepository, never())
                .save(any());

        verifyNoInteractions(
                domainEventPublisher
        );
    }

    private Withdrawal createProcessingWithdrawal() {

        Withdrawal withdrawal =
                Withdrawal.create(
                        walletId,
                        providerId,
                        amount
                );

        withdrawal.startProcessing();

        return withdrawal;
    }
}