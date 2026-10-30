package com.pm.paymentservicenew.api.controller;

import com.pm.paymentservicenew.api.response.ApiResponse;
import com.pm.paymentservicenew.application.dto.request.CreatePaymentRequest;
import com.pm.paymentservicenew.application.dto.response.PaymentInitializationResponse;
import com.pm.paymentservicenew.application.dto.response.PaymentResponse;
import com.pm.paymentservicenew.application.service.PaymentService;
import com.pm.paymentservicenew.security.SecurityUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@PreAuthorize("hasRole('USER')")
public class PaymentController {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService
    ) {
        this.paymentService = paymentService;
    }

    // -------------------------
    // INITIALIZE PAYMENT
    // -------------------------

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentInitializationResponse>> createPayment(

            @Valid
            @RequestBody
            CreatePaymentRequest request

    ) {

        UUID customerId = SecurityUtils.getCurrentUserId();

        log.info("========== PAYMENT INITIALIZATION REQUEST ==========");
        log.info("Booking ID : {}", request.bookingId());
        log.info("Customer ID : {}", customerId);

        PaymentInitializationResponse response =
                paymentService.createPayment(
                        request,
                        customerId
                );

        log.info("Payment initialized successfully.");
        log.info("===================================================");

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }


    // -------------------------
    // GET PAYMENT
    // -------------------------

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(

            @PathVariable
            UUID paymentId

    ) {

        UUID customerId = SecurityUtils.getCurrentUserId();

        log.info(
                "Fetching payment {} for customer {}",
                paymentId,
                customerId
        );

        PaymentResponse response =
                paymentService.getPayment(
                        customerId,
                        paymentId
                );

        log.info(
                "Payment {} retrieved successfully.",
                paymentId
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    // -------------------------
    // GET PAYMENT FOR BOOKING
    // -------------------------

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBooking(

            @PathVariable
            UUID bookingId

    ) {

        UUID customerId = SecurityUtils.getCurrentUserId();

        log.info(
                "Fetching payment for booking {} and customer {}",
                bookingId,
                customerId
        );

        PaymentResponse response =
                paymentService.getPaymentByBooking(
                        customerId,
                        bookingId
                );

        log.info(
                "Payment for booking {} retrieved successfully.",
                bookingId
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    // -------------------------
    // GET MY PAYMENTS
    // -------------------------

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments() {

        UUID customerId = SecurityUtils.getCurrentUserId();

        log.info(
                "Fetching all payments for customer {}",
                customerId
        );

        List<PaymentResponse> response =
                paymentService.getCustomerPayments(customerId);

        log.info(
                "Retrieved {} payment(s).",
                response.size()
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}