
package com.pm.paymentservicenew.domain.repository;

import com.pm.paymentservicenew.domain.model.escrow.EscrowRelease;
import com.pm.paymentservicenew.domain.model.escrow.EscrowReleaseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EscrowReleaseRepository
        extends JpaRepository<EscrowRelease, UUID> {

    List<EscrowRelease> findByEscrowIdOrderByReleasedAtAsc(
            UUID escrowId
    );

    List<EscrowRelease> findByBookingIdOrderByReleasedAtAsc(
            UUID bookingId
    );

    List<EscrowRelease> findByProviderIdOrderByReleasedAtDesc(
            UUID providerId
    );

    /**
     * Finds the existing release for a particular business operation.
     * The escrow lock in EscrowRepository protects this check from
     * concurrent release requests.
     */
    List<EscrowRelease> findByBookingIdAndReleaseType(
            UUID bookingId,
            EscrowReleaseType releaseType
    );
}
