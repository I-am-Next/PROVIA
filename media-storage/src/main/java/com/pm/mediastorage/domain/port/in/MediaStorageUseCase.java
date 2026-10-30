package com.pm.mediastorage.domain.port.in;

import com.pm.mediastorage.application.model.MediaContentResult;
import com.pm.mediastorage.application.model.MediaDeliveryResult;
import com.pm.mediastorage.domain.model.MediaPage;
import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageUseCase {

    MediaDeliveryResult uploadProfilePhoto(
            MultipartFile file
    );

    MediaDeliveryResult uploadPortfolioImage(
            MultipartFile file
    );

    MediaDeliveryResult uploadDocument(
            MultipartFile file
    );

    MediaDeliveryResult getMedia(
            String mediaId
    );

    MediaContentResult getMediaContent(
            String mediaId
    );

    MediaPage getPublicPortfolio(
            String ownerId,
            int page,
            int size
    );

    void deleteMedia(
            String mediaId
    );
}