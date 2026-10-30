package com.pm.bookingservice.domain.repository;

import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findByUserId(
            UUID userId,
            Pageable pageable
    );

    Page<Booking> findByUserIdAndStatusIn(
            UUID userId,
            Collection<BookingStatus> statuses,
            Pageable pageable
    );

    Page<Booking> findByProviderUserId(
            UUID providerUserId,
            Pageable pageable
    );

    Page<Booking> findByProviderUserIdAndStatus(
            UUID providerUserId,
            BookingStatus status,
            Pageable pageable
    );

    Page<Booking> findByStatus(
            BookingStatus status,
            Pageable pageable
    );

    boolean existsByIdAndUserId(
            UUID bookingId,
            UUID userId
    );

    boolean existsByIdAndProviderUserId(
            UUID bookingId,
            UUID providerUserId
    );

    long countByUserId(
            UUID userId
    );

    long countByUserIdAndStatusIn(
            UUID userId,
            Collection<BookingStatus> statuses
    );

    List<Booking> findByUserIdAndScheduledStartAfterAndStatusNotInOrderByScheduledStartAsc(
            UUID userId,
            LocalDateTime now,
            Collection<BookingStatus> statuses
    );

    List<Booking> findTop5ByUserIdOrderByCreatedAtDesc(
            UUID userId
    );
}