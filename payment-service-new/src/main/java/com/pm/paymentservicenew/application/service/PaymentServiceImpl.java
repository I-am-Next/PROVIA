package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.request.CreatePaymentRequest;
import com.pm.paymentservicenew.application.dto.response.PaymentInitializationResponse;
import com.pm.paymentservicenew.application.dto.response.PaymentResponse;
import com.pm.paymentservicenew.application.event.DomainEventPublisher;
import com.pm.paymentservicenew.application.factory.EscrowEventFactory;
import com.pm.paymentservicenew.application.factory.PaymentEventFactory;
import com.pm.paymentservicenew.application.mapper.PaymentMapper;
import com.pm.paymentservicenew.domain.exception.PaymentAlreadyExistsException;
import com.pm.paymentservicenew.domain.exception.PaymentNotFoundException;
import com.pm.paymentservicenew.domain.model.escrow.Escrow;
import com.pm.paymentservicenew.domain.model.payment.Payment;
import com.pm.paymentservicenew.domain.repository.EscrowRepository;
import com.pm.paymentservicenew.domain.repository.PaymentRepository;
import com.pm.paymentservicenew.infrastructure.client.BookingClient;
import com.pm.paymentservicenew.integration.dto.BookingStatus;
import com.pm.paymentservicenew.integration.paystack.dto.VerifyTransactionResponse;
import com.pm.paymentservicenew.integration.paystack.service.PaymentGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGatewayService paymentGatewayService;
    private final BookingClient bookingClient;
    private final EscrowRepository escrowRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final PaymentEventFactory paymentEventFactory;
    private final EscrowEventFactory escrowEventFactory;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentMapper paymentMapper,
            PaymentGatewayService paymentGatewayService,
            PaymentEventFactory paymentEventFactory,
            BookingClient bookingClient,
            EscrowRepository escrowRepository,
            DomainEventPublisher domainEventPublisher,
            EscrowEventFactory escrowEventFactory
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.paymentGatewayService = paymentGatewayService;
        this.paymentEventFactory = paymentEventFactory;
        this.bookingClient = bookingClient;
        this.escrowRepository = escrowRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.escrowEventFactory = escrowEventFactory;
    }

    @Override
    public PaymentInitializationResponse createPayment(
            CreatePaymentRequest request,
            UUID customerId
    ) {

        try {

            log.info(
                    "Initializing payment for booking {}",
                    request.bookingId()
            );

            var booking = bookingClient.getBooking(
                    request.bookingId()
            );

            /*
             * Authorization
             *
             * The authenticated customer must own
             * the booking before a payment can be
             * initialized.
             *
             * We deliberately return a not-found style
             * exception rather than exposing that another
             * customer's booking exists.
             */
            if (!booking.customerId().equals(customerId)) {

                log.warn(
                        "Payment initialization rejected for booking {}.",
                        request.bookingId()
                );

                throw new PaymentNotFoundException(
                        request.bookingId(),
                        "booking"
                );
            }

            if (booking.status() != BookingStatus.QUOTE_ACCEPTED) {

                log.warn(
                        "Payment initialization rejected for booking {}. " +
                                "Current status={}",
                        request.bookingId(),
                        booking.status()
                );

                throw new IllegalStateException(
                        "Payment can only be initialized for bookings with QUOTE_ACCEPTED status."
                );
            }

            if (paymentRepository.existsByBookingId(
                    booking.bookingId()
            )) {

                log.warn(
                        "Payment already exists for booking {}",
                        booking.bookingId()
                );

                throw new PaymentAlreadyExistsException(
                        booking.bookingId()
                );
            }

            Payment payment = Payment.create(
                    booking.bookingId(),
                    booking.customerId(),
                    booking.providerId(),
                    booking.amount()
            );

            paymentRepository.save(payment);

            var gatewayResponse =
                    paymentGatewayService.initializePayment(
                            payment.getId(),
                            request.customerEmail(),
                            payment.getAmount()
                    );

            log.info(
                    "Payment initialized successfully. paymentId={}, bookingId={}, reference={}",
                    payment.getId(),
                    payment.getBookingId(),
                    gatewayResponse.reference()
            );

            return new PaymentInitializationResponse(
                    paymentMapper.toResponse(payment),
                    gatewayResponse.authorizationUrl(),
                    gatewayResponse.reference()
            );

        } catch (Exception ex) {

            log.error(
                    "Payment initialization failed for booking {}.",
                    request.bookingId(),
                    ex
            );

            throw ex;
        }
    }

    @Override
    public void verifyPayment(String reference) {

        log.info(
                "Payment verification started. reference={}",
                reference
        );

        UUID paymentId = UUID.fromString(reference);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(
                        () -> new PaymentNotFoundException(
                                paymentId,
                                "payment"
                        )
                );

        /*
         * Idempotency
         */
        if (!payment.isPending()) {

            log.info(
                    "Payment verification skipped. reference={}, status={}",
                    reference,
                    payment.getStatus()
            );

            return;
        }

        try {

            VerifyTransactionResponse verification =
                    paymentGatewayService.verifyPayment(
                            reference
                    );

            /*
             * Validate the gateway response against
             * the local payment before changing any
             * local payment state.
             */
            validateGatewayVerification(
                    payment,
                    reference,
                    verification
            );

            /*
             * Persist gateway information only after
             * the verification has passed.
             */
            payment.assignGatewayReference(
                    verification.reference()
            );

            /*
             * Complete payment.
             */
            payment.complete(
                    Instant.parse(
                            verification.paid_at()
                    )
            );

            paymentRepository.save(payment);

            log.info(
                    "Payment marked COMPLETED. paymentId={}, bookingId={}",
                    payment.getId(),
                    payment.getBookingId()
            );

            /*
             * Create Escrow.
             */
            Escrow escrow = Escrow.create(
                    payment.getId(),
                    payment.getBookingId(),
                    payment.getCustomerId(),
                    payment.getProviderId(),
                    payment.getAmount()
            );

            escrowRepository.save(escrow);

            log.info(
                    "Escrow created. escrowId={}, paymentId={}",
                    escrow.getId(),
                    payment.getId()
            );

            /*
             * Publish domain events.
             */
            domainEventPublisher.publish(
                    paymentEventFactory.paymentCompleted(
                            payment,
                            escrow
                    )
            );

            domainEventPublisher.publish(
                    escrowEventFactory.escrowCreated(
                            escrow
                    )
            );

            log.info(
                    "Payment and escrow events published. paymentId={}, escrowId={}",
                    payment.getId(),
                    escrow.getId()
            );

        } catch (Exception ex) {

            log.error(
                    "Payment verification failed. reference={}",
                    reference,
                    ex
            );

            /*
             * Only mark the payment FAILED when it
             * is still pending.
             *
             * This preserves the existing state-machine
             * invariant if an exception occurs.
             */
            if (payment.isPending()) {

                payment.fail();

                paymentRepository.save(payment);

                log.info(
                        "Payment marked FAILED. paymentId={}",
                        payment.getId()
                );
            }

            throw ex;
        }

        log.info(
                "Payment verification completed. reference={}",
                reference
        );
    }

    private void validateGatewayVerification(
            Payment payment,
            String expectedReference,
            VerifyTransactionResponse verification
    ) {

        if (verification == null) {

            throw new IllegalStateException(
                    "Payment gateway returned an empty verification response."
            );
        }

        /*
         * 1. Gateway transaction must be successful.
         */
        if (!"success".equalsIgnoreCase(
                verification.status()
        )) {

            throw new IllegalStateException(
                    "Payment gateway verification was not successful."
            );
        }

        /*
         * 2. Gateway reference must match the local
         *    payment reference being verified.
         */
        if (!expectedReference.equals(
                verification.reference()
        )) {

            throw new IllegalStateException(
                    "Payment gateway reference does not match the payment reference."
            );
        }

        /*
         * 3. Gateway currency must match the local
         *    payment currency.
         */
        if (!payment.getCurrency().equalsIgnoreCase(
                verification.currency()
        )) {

            throw new IllegalStateException(
                    "Payment gateway currency does not match the payment currency."
            );
        }

        /*
         * 4. Paystack amount is represented in kobo,
         *    while the local payment amount is stored
         *    in naira.
         *
         *    Example:
         *
         *    Local:   50,000.00 NGN
         *    Gateway: 5,000,000 kobo
         */
        BigDecimal expectedGatewayAmount =
                payment.getAmount()
                        .movePointRight(2);

        BigDecimal actualGatewayAmount =
                BigDecimal.valueOf(
                        verification.amount()
                );

        if (expectedGatewayAmount.compareTo(
                actualGatewayAmount
        ) != 0) {

            throw new IllegalStateException(
                    "Payment gateway amount does not match the payment amount."
            );
        }

        log.info(
                "Gateway verification validated. reference={}, currency={}, status={}",
                verification.reference(),
                verification.currency(),
                verification.status()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(
            UUID customerId,
            UUID paymentId
    ) {

        Payment payment = getPaymentOrThrow(
                paymentId
        );

        if (!payment.getCustomerId().equals(
                customerId
        )) {

            throw new PaymentNotFoundException(
                    paymentId,
                    "payment"
            );
        }

        return paymentMapper.toResponse(
                payment
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBooking(
            UUID customerId,
            UUID bookingId
    ) {

        Payment payment =
                paymentRepository.findByBookingId(
                        bookingId
                ).orElseThrow(
                        () -> new PaymentNotFoundException(
                                bookingId,
                                "booking"
                        )
                );

        if (!payment.getCustomerId().equals(
                customerId
        )) {

            throw new PaymentNotFoundException(
                    bookingId,
                    "booking"
            );
        }

        return paymentMapper.toResponse(
                payment
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getCustomerPayments(
            UUID customerId
    ) {

        return paymentRepository
                .findByCustomerId(customerId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    private Payment getPaymentOrThrow(
            UUID paymentId
    ) {

        return paymentRepository.findById(
                paymentId
        ).orElseThrow(
                () -> new PaymentNotFoundException(
                        paymentId,
                        "payment"
                )
        );
    }
}