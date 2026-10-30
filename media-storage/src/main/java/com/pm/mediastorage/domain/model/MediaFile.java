package com.pm.mediastorage.domain.model;

import java.time.Instant;

public record MediaFile(

        String id,

        String objectKey,

        String originalFilename,

        String contentType,

        long size,

        MediaType mediaType,

        MediaVisibility visibility,

        String ownerId,

        Instant uploadedAt

) {
}