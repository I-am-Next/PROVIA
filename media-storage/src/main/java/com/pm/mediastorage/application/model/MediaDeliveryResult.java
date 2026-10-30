package com.pm.mediastorage.application.model;

import com.pm.mediastorage.domain.model.MediaFile;

public record MediaDeliveryResult(

        MediaFile mediaFile,

        DeliveryType deliveryType,

        String url
) {
}