package com.pm.paymentservicenew.domain.repository;

import com.pm.paymentservicenew.domain.model.withdrawal.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, UUID> {

    Optional<Withdrawal> findByIdAndProviderId(

            UUID withdrawalId,

            UUID providerId

    );

    List<Withdrawal> findByProviderId(

            UUID providerId

    );
}