package com.pm.mediastorage.application.model;

import com.pm.mediastorage.domain.model.MediaFile;
import com.pm.mediastorage.domain.port.out.StorageContent;

public record MediaContentResult(

        MediaFile mediaFile,

        StorageContent storageContent
) {
}