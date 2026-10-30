package com.pm.notificationservice.application.factory.booking;

import com.pm.notificationservice.application.factory.shared.BaseNotificationFactory;
import com.pm.notificationservice.application.factory.shared.NotificationMessages;
import com.pm.notificationservice.application.factory.shared.NotificationActionUrlFactory;
import com.pm.notificationservice.application.factory.shared.NotificationMetadataFactory;
import com.pm.notificationservice.application.factory.shared.NotificationPriorityFactory;
import com.pm.notificationservice.application.factory.shared.NotificationTitles;
import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import com.pm.contracts.common.enums.RecipientRole;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BookingNotificationFactory extends BaseNotificationFactory {

    public BookingNotificationFactory(

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

    public Notification bookingCreated(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.BOOKING_CREATED,

                NotificationTitles.BOOKING_CREATED,

                NotificationMessages.bookingCreated(),

                bookingId,

                null,

                null,

                actionUrlFactory.booking(

                        bookingId

                ),

                metadataFactory.bookingMetadata(

                        bookingId

                )

        );

    }

    public Notification bookingAccepted(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.BOOKING_ACCEPTED,

                NotificationTitles.BOOKING_ACCEPTED,

                NotificationMessages.bookingAccepted(),

                bookingId,

                null,

                null,

                actionUrlFactory.booking(

                        bookingId

                ),

                metadataFactory.bookingMetadata(

                        bookingId

                )

        );

    }

    public Notification bookingRejected(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.BOOKING_REJECTED,

                NotificationTitles.BOOKING_REJECTED,

                NotificationMessages.bookingRejected(),

                bookingId,

                null,

                null,

                actionUrlFactory.booking(

                        bookingId

                ),

                metadataFactory.bookingMetadata(

                        bookingId

                )

        );

    }

    public Notification bookingCancelled(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.BOOKING_CANCELLED,

                NotificationTitles.BOOKING_CANCELLED,

                NotificationMessages.bookingCancelled(),

                bookingId,

                null,

                null,

                actionUrlFactory.booking(

                        bookingId

                ),

                metadataFactory.bookingMetadata(

                        bookingId

                )

        );

    }

    public Notification quoteReceived(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.QUOTE_RECEIVED,

                NotificationTitles.QUOTE_RECEIVED,

                NotificationMessages.quoteReceived(),

                bookingId,

                null,

                null,

                actionUrlFactory.booking(

                        bookingId

                ),

                metadataFactory.bookingMetadata(

                        bookingId

                )

        );

    }

    public Notification quoteAccepted(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.QUOTE_ACCEPTED,

                NotificationTitles.QUOTE_ACCEPTED,

                NotificationMessages.quoteAccepted(),

                bookingId,

                null,

                null,

                actionUrlFactory.booking(

                        bookingId

                ),

                metadataFactory.bookingMetadata(

                        bookingId

                )

        );

    }

    public Notification quoteRejected(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.QUOTE_REJECTED,

                NotificationTitles.QUOTE_REJECTED,

                NotificationMessages.quoteRejected(),

                bookingId,

                null,

                null,

                actionUrlFactory.booking(

                        bookingId

                ),

                metadataFactory.bookingMetadata(

                        bookingId

                )

        );

    }

    public Notification workStarted(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.WORK_STARTED,

                NotificationTitles.WORK_STARTED,

                NotificationMessages.workStarted(),

                bookingId,

                null,

                null,

                actionUrlFactory.booking(

                        bookingId

                ),

                metadataFactory.bookingMetadata(

                        bookingId

                )

        );

    }

    public Notification workCompleted(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.WORK_COMPLETED,

                NotificationTitles.WORK_COMPLETED,

                NotificationMessages.workCompleted(),

                bookingId,

                null,

                null,

                actionUrlFactory.booking(

                        bookingId

                ),

                metadataFactory.bookingMetadata(

                        bookingId

                )

        );

    }

    public Notification customerConfirmedCompletion(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.CUSTOMER_CONFIRMED_COMPLETION,

                NotificationTitles.CUSTOMER_CONFIRMED_COMPLETION,

                NotificationMessages.customerConfirmedCompletion(),

                bookingId,

                null,

                null,

                actionUrlFactory.booking(

                        bookingId

                ),

                metadataFactory.bookingMetadata(

                        bookingId

                )

        );

    }

}