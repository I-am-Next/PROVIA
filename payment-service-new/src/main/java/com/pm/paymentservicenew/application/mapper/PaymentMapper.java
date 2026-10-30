package com.pm.paymentservicenew.application.mapper;

import com.pm.paymentservicenew.application.dto.response.PaymentResponse;
import com.pm.paymentservicenew.domain.model.payment.Payment;
import org.springframework.stereotype.Component;

@Component
public record PaymentMapper() {

    public PaymentResponse toResponse(
            Payment payment
    ) {

        return new PaymentResponse(

                payment.getId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt()

        );

    }

}