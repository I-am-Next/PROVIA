package com.pm.notificationservice.infrastructure.consumer.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.EventEnvelope;
import com.pm.contracts.payment.PaymentCompletedEvent;
import com.pm.contracts.payment.PaymentFailedEvent;
import com.pm.contracts.payment.PaymentPendingEvent;
import com.pm.notificationservice.application.service.EventProcessingService;
import com.pm.notificationservice.application.service.NotificationCreationService;
import com.pm.notificationservice.infrastructure.consumer.AbstractEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventHandler
        extends AbstractEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PaymentEventHandler.class
            );

    public PaymentEventHandler(
            ObjectMapper objectMapper,
            NotificationCreationService notificationCreationService,
            EventProcessingService eventProcessingService
    ) {

        super(
                objectMapper,
                notificationCreationService,
                eventProcessingService
        );
    }

    public void handle(
            EventEnvelope<?> envelope
    ) {

        log.info(
                ">>> PaymentEventHandler.handle() ENTERED. " +
                        "type={}, version={}, payloadClass={}",
                envelope.type(),
                envelope.version(),
                envelope.payload() != null
                        ? envelope.payload()
                        .getClass()
                        .getSimpleName()
                        : "null"
        );

        switch (envelope.type()) {

            case "PAYMENT_PENDING" -> {

                log.info(
                        ">>> PAYMENT_PENDING case entered"
                );

                PaymentPendingEvent event =
                        convert(
                                envelope.payload(),
                                PaymentPendingEvent.class
                        );

                log.info(
                        ">>> PAYMENT_PENDING payload converted successfully. " +
                                "paymentId={}, bookingId={}",
                        event.paymentId(),
                        event.bookingId()
                );

                process(
                        event,
                        () -> {

                            log.info(
                                    ">>> Calling createPaymentPendingNotification(). " +
                                            "paymentId={}",
                                    event.paymentId()
                            );

                            notificationCreationService
                                    .createPaymentPendingNotification(
                                            event
                                    );

                            log.info(
                                    ">>> createPaymentPendingNotification() completed. " +
                                            "paymentId={}",
                                    event.paymentId()
                            );
                        }
                );

                log.info(
                        ">>> PAYMENT_PENDING case completed. eventId={}",
                        event.eventId()
                );
            }

            case "PAYMENT_COMPLETED" -> {

                log.info(
                        ">>> PAYMENT_COMPLETED case entered"
                );

                PaymentCompletedEvent event =
                        convert(
                                envelope.payload(),
                                PaymentCompletedEvent.class
                        );

                log.info(
                        ">>> PAYMENT_COMPLETED payload converted successfully. " +
                                "paymentId={}, bookingId={}, escrowId={}",
                        event.paymentId(),
                        event.bookingId(),
                        event.escrowId()
                );

                process(
                        event,
                        () -> {

                            log.info(
                                    ">>> Calling createPaymentCompletedNotification(). " +
                                            "paymentId={}",
                                    event.paymentId()
                            );

                            notificationCreationService
                                    .createPaymentCompletedNotification(
                                            event
                                    );

                            log.info(
                                    ">>> createPaymentCompletedNotification() completed. " +
                                            "paymentId={}",
                                    event.paymentId()
                            );
                        }
                );

                log.info(
                        ">>> PAYMENT_COMPLETED case completed. eventId={}",
                        event.eventId()
                );
            }

            case "PAYMENT_FAILED" -> {

                log.info(
                        ">>> PAYMENT_FAILED case entered"
                );

                PaymentFailedEvent event =
                        convert(
                                envelope.payload(),
                                PaymentFailedEvent.class
                        );

                log.info(
                        ">>> PAYMENT_FAILED payload converted successfully. " +
                                "paymentId={}, bookingId={}",
                        event.paymentId(),
                        event.bookingId()
                );

                process(
                        event,
                        () -> {

                            log.info(
                                    ">>> Calling createPaymentFailedNotification(). " +
                                            "paymentId={}",
                                    event.paymentId()
                            );

                            notificationCreationService
                                    .createPaymentFailedNotification(
                                            event
                                    );

                            log.info(
                                    ">>> createPaymentFailedNotification() completed. " +
                                            "paymentId={}",
                                    event.paymentId()
                            );
                        }
                );

                log.info(
                        ">>> PAYMENT_FAILED case completed. eventId={}",
                        event.eventId()
                );
            }

            default -> {

                log.warn(
                        ">>> UNKNOWN PAYMENT EVENT TYPE: {}",
                        envelope.type()
                );
            }
        }

        log.info(
                ">>> PaymentEventHandler.handle() EXITED. type={}",
                envelope.type()
        );
    }
}