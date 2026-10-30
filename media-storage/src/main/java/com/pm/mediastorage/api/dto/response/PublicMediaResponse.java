package com.pm.mediastorage.api.dto.response;

import com.pm.mediastorage.domain.model.MediaFile;

import java.time.Instant;

public record PublicMediaResponse(

        String mediaId,

        String contentType,

        long size,

        String url,

        Instant uploadedAt

) {

    public static PublicMediaResponse from(
            MediaFile mediaFile
    ) {

        return new PublicMediaResponse(
                mediaFile.id(),
                mediaFile.contentType(),
                mediaFile.size(),
                "/api/v1/media/"
                        + mediaFile.id()
                        + "/content",
                mediaFile.uploadedAt()
        );
    }
}