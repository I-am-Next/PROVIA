package com.pm.mediastorage.api.dto.response;

import com.pm.mediastorage.domain.model.MediaFile;
import com.pm.mediastorage.domain.model.MediaType;
import com.pm.mediastorage.domain.model.MediaVisibility;

import java.time.Instant;

public record MediaFileResponse(

        String mediaId,

        String originalFilename,

        String contentType,

        long size,

        MediaType mediaType,

        MediaVisibility visibility,

        String url,

        Instant uploadedAt
) {

    public static MediaFileResponse from(
            MediaFile mediaFile,
            String url
    ) {

        return new MediaFileResponse(
                mediaFile.id(),
                mediaFile.originalFilename(),
                mediaFile.contentType(),
                mediaFile.size(),
                mediaFile.mediaType(),
                mediaFile.visibility(),
                url,
                mediaFile.uploadedAt()
        );
    }
}