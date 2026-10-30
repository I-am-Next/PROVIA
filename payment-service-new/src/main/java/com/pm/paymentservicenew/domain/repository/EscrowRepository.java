
package com.pm.paymentservicenew.domain.repository;

import com.pm.paymentservicenew.domain.model.escrow.Escrow;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EscrowRepository extends JpaRepository<Escrow, UUID> {

    Optional<Escrow> findByPaymentId(UUID paymentId);

    Optional<Escrow> findByBookingId(UUID bookingId);

    /**
     * Serializes financial operations for a booking's escrow.
     *
     * Concurrent release requests for the same escrow must wait until
     * the current transaction commits or rolls back.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT e
            FROM Escrow e
            WHERE e.bookingId = :bookingId
            """)
    Optional<Escrow> findByBookingIdForUpdate(
            @Param("bookingId") UUID bookingId
    );
}
