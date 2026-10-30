package com.pm.mediastorage.domain.port.out;

import com.pm.mediastorage.domain.model.MediaFile;
import com.pm.mediastorage.domain.model.MediaPage;
import com.pm.mediastorage.domain.model.MediaType;

import java.util.Optional;

public interface MediaMetadataPort {

    MediaFile save(
            MediaFile mediaFile
    );

    Optional<MediaFile> findById(
            String mediaId
    );

    MediaPage findPublicMediaByOwnerIdAndType(
            String ownerId,
            MediaType mediaType,
            int page,
            int size
    );

    void deleteById(
            String mediaId
    );
}