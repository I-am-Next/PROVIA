package com.pm.notificationservice.application.factory.escrow;

import com.pm.notificationservice.application.factory.shared.BaseNotificationFactory;
import com.pm.notificationservice.application.factory.shared.NotificationActionUrlFactory;
import com.pm.notificationservice.application.factory.shared.NotificationMessages;
import com.pm.notificationservice.application.factory.shared.NotificationMetadataFactory;
import com.pm.notificationservice.application.factory.shared.NotificationPriorityFactory;
import com.pm.notificationservice.application.factory.shared.NotificationTitles;
import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import com.pm.contracts.common.enums.RecipientRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class EscrowNotificationFactory extends BaseNotificationFactory {

    public EscrowNotificationFactory(

            NotificationMetadataFactory metadataFactory,

            NotificationActionUrlFactory actionUrlFactory,

            NotificationPriorityFactory priorityFactory

    ) {

        super(

                metadataFactory,

                actionUrlFactory,

                priorityFactory

        );

    }

    public Notification escrowCreated(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId,

            UUID paymentId,

            UUID escrowId,

            BigDecimal amount

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.ESCROW_CREATED,

                NotificationTitles.ESCROW_CREATED,

                NotificationMessages.escrowCreated(),

                bookingId,

                paymentId,

                escrowId,

                actionUrlFactory.escrow(

                        escrowId

                ),

                metadataFactory.escrowMetadata(

                        bookingId,

                        paymentId,

                        escrowId,

                        amount

                )

        );

    }

    public Notification materialsReleased(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId,

            UUID paymentId,

            UUID escrowId,

            BigDecimal amount

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.MATERIALS_RELEASED,

                NotificationTitles.MATERIALS_RELEASED,

                NotificationMessages.materialsReleased(

                        amount

                ),

                bookingId,

                paymentId,

                escrowId,

                actionUrlFactory.escrow(

                        escrowId

                ),

                metadataFactory.escrowMetadata(

                        bookingId,

                        paymentId,

                        escrowId,

                        amount

                )

        );

    }

    public Notification labourReleased(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId,

            UUID paymentId,

            UUID escrowId,

            BigDecimal amount

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.LABOUR_RELEASED,

                NotificationTitles.LABOUR_RELEASED,

                NotificationMessages.labourReleased(

                        amount

                ),

                bookingId,

                paymentId,

                escrowId,

                actionUrlFactory.escrow(

                        escrowId

                ),

                metadataFactory.escrowMetadata(

                        bookingId,

                        paymentId,

                        escrowId,

                        amount

                )

        );

    }

    public Notification escrowRefunded(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId,

            UUID paymentId,

            UUID escrowId,

            BigDecimal amount

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.ESCROW_REFUNDED,

                NotificationTitles.ESCROW_REFUNDED,

                NotificationMessages.escrowRefunded(

                        amount

                ),

                bookingId,

                paymentId,

                escrowId,

                actionUrlFactory.escrow(

                        escrowId

                ),

                metadataFactory.escrowMetadata(

                        bookingId,

                        paymentId,

                        escrowId,

                        amount

                )

        );

    }

}