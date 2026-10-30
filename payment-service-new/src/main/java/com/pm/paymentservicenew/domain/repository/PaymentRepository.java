package com.pm.paymentservicenew.domain.repository;

import com.pm.paymentservicenew.domain.model.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByBookingId(
            UUID bookingId
    );

    boolean existsByBookingId(
            UUID bookingId
    );

    List<Payment> findByCustomerId(
            UUID customerId
    );


}