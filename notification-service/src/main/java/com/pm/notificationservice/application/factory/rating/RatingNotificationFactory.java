package com.pm.notificationservice.application.factory.rating;

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

import java.util.UUID;

@Component
public class RatingNotificationFactory extends BaseNotificationFactory {

    public RatingNotificationFactory(

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

    public Notification providerRated(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId,

            UUID ratingId,

            Integer rating

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.PROVIDER_RATED,

                NotificationTitles.PROVIDER_RATED,

                NotificationMessages.providerRated(

                        rating

                ),

                bookingId,

                null,

                null,

                actionUrlFactory.rating(

                        ratingId

                ),

                metadataFactory.ratingMetadata(

                        bookingId,

                        ratingId

                )

        );

    }

    public Notification customerRated(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID bookingId,

            UUID ratingId

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.CUSTOMER_RATED,

                NotificationTitles.CUSTOMER_RATED,

                NotificationMessages.customerRated(),

                bookingId,

                null,

                null,

                actionUrlFactory.rating(

                        ratingId

                ),

                metadataFactory.ratingMetadata(

                        bookingId,

                        ratingId

                )

        );

    }

}