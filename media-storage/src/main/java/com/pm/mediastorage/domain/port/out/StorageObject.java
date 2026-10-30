package com.pm.mediastorage.domain.port.out;

import com.pm.mediastorage.domain.model.MediaType;
import com.pm.mediastorage.domain.model.MediaVisibility;

import java.io.InputStream;

public record StorageObject(

        String mediaId,

        String ownerId,

        String objectKey,

        MediaType mediaType,

        MediaVisibility visibility,

        String originalFilename,

        String contentType,

        String extension,

        long size,

        InputStream content

) {
}