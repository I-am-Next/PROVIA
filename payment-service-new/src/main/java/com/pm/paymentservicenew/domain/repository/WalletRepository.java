
package com.pm.paymentservicenew.domain.repository;

import com.pm.paymentservicenew.domain.model.wallet.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByProviderId(UUID providerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w
            FROM Wallet w
            WHERE w.providerId = :providerId
            """)
    Optional<Wallet> findByProviderIdForUpdate(
            @Param("providerId") UUID providerId
    );
}
