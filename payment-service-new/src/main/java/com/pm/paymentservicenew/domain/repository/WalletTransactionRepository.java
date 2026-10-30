
package com.pm.paymentservicenew.domain.repository;

import com.pm.paymentservicenew.domain.model.wallet.WalletTransaction;
import com.pm.paymentservicenew.domain.model.wallet.WalletTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, UUID> {

    List<WalletTransaction> findByWalletIdOrderByOccurredAtDesc(
            UUID walletId
    );

    Optional<WalletTransaction> findByReferenceIdAndType(
            UUID referenceId,
            WalletTransactionType type
    );
}
