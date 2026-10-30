package com.pm.mediastorage.application.service.impl;

import com.pm.mediastorage.application.model.DeliveryType;
import com.pm.mediastorage.application.model.MediaContentResult;
import com.pm.mediastorage.application.model.MediaDeliveryResult;
import com.pm.mediastorage.application.service.MediaStorageService;
import com.pm.mediastorage.config.StorageProperties;
import com.pm.mediastorage.domain.model.MediaFile;
import com.pm.mediastorage.domain.model.MediaPage;
import com.pm.mediastorage.domain.model.MediaType;
import com.pm.mediastorage.domain.model.MediaVisibility;
import com.pm.mediastorage.domain.port.out.CurrentUserPort;
import com.pm.mediastorage.domain.port.out.MediaMetadataPort;
import com.pm.mediastorage.domain.port.out.StorageContent;
import com.pm.mediastorage.domain.port.out.StorageObject;
import com.pm.mediastorage.domain.port.out.StoragePort;
import com.pm.mediastorage.exception.InvalidMediaFileException;
import com.pm.mediastorage.exception.MediaAccessDeniedException;
import com.pm.mediastorage.exception.MediaNotFoundException;
import com.pm.mediastorage.exception.MediaTooLargeException;
import com.pm.mediastorage.exception.StorageException;
import com.pm.mediastorage.exception.UnsupportedMediaTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MediaStorageServiceImpl
        implements MediaStorageService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MediaStorageServiceImpl.class
            );

    private static final int DEFAULT_PAGE_SIZE = 24;

    private static final int MAX_PAGE_SIZE = 100;

    private final StoragePort storagePort;

    private final MediaMetadataPort mediaMetadataPort;

    private final CurrentUserPort currentUserPort;

    private final StorageProperties storageProperties;

    public MediaStorageServiceImpl(
            StoragePort storagePort,
            MediaMetadataPort mediaMetadataPort,
            CurrentUserPort currentUserPort,
            StorageProperties storageProperties
    ) {

        this.storagePort = storagePort;
        this.mediaMetadataPort = mediaMetadataPort;
        this.currentUserPort = currentUserPort;
        this.storageProperties = storageProperties;
    }

    @Override
    public MediaDeliveryResult uploadProfilePhoto(
            MultipartFile file
    ) {

        String ownerId =
                currentUserPort.getCurrentUserId();

        validateOwner(ownerId);

        return uploadMedia(
                ownerId,
                file,
                MediaType.PROFILE_PHOTO,
                MediaVisibility.PUBLIC
        );
    }

    @Override
    public MediaDeliveryResult uploadPortfolioImage(
            MultipartFile file
    ) {

        String ownerId =
                currentUserPort.getCurrentUserId();

        validateOwner(ownerId);

        return uploadMedia(
                ownerId,
                file,
                MediaType.PORTFOLIO_IMAGE,
                MediaVisibility.PUBLIC
        );
    }

    @Override
    public MediaDeliveryResult uploadDocument(
            MultipartFile file
    ) {

        String ownerId =
                currentUserPort.getCurrentUserId();

        validateOwner(ownerId);

        return uploadMedia(
                ownerId,
                file,
                MediaType.DOCUMENT,
                MediaVisibility.PRIVATE
        );
    }

    @Override
    public MediaDeliveryResult getMedia(
            String mediaId
    ) {

        MediaFile mediaFile =
                findMediaById(mediaId);

        if (mediaFile.visibility()
                == MediaVisibility.PUBLIC) {

            return new MediaDeliveryResult(
                    mediaFile,
                    DeliveryType.URL,
                    buildDeliveryUrl(mediaFile)
            );
        }

        return new MediaDeliveryResult(
                mediaFile,
                DeliveryType.STREAM,
                null
        );
    }

    @Override
    public MediaContentResult getMediaContent(
            String mediaId
    ) {

        MediaFile mediaFile =
                findMediaById(mediaId);

        authorizeMediaAccess(
                mediaFile
        );

        StorageContent storageContent =
                storagePort.retrieve(
                        mediaFile.objectKey()
                );

        return new MediaContentResult(
                mediaFile,
                storageContent
        );
    }

    @Override
    public MediaPage getPublicPortfolio(
            String ownerId,
            int page,
            int size
    ) {

        validateOwnerId(ownerId);

        int normalizedPage =
                Math.max(
                        page,
                        0
                );

        int normalizedSize =
                normalizePageSize(size);

        return mediaMetadataPort
                .findPublicMediaByOwnerIdAndType(
                        ownerId,
                        MediaType.PORTFOLIO_IMAGE,
                        normalizedPage,
                        normalizedSize
                );
    }

    @Override
    public void deleteMedia(
            String mediaId
    ) {

        String currentUserId =
                currentUserPort.getCurrentUserId();

        validateOwner(currentUserId);

        MediaFile mediaFile =
                findMediaById(mediaId);

        if (!mediaFile.ownerId()
                .equals(currentUserId)) {

            throw new MediaAccessDeniedException(
                    "You are not allowed to delete this media"
            );
        }

        try {

            mediaMetadataPort.deleteById(
                    mediaId
            );

        } catch (RuntimeException exception) {

            log.error(
                    "Media metadata deletion failed. Physical media was not deleted. " +
                            "mediaId={}, objectKey={}",
                    mediaId,
                    mediaFile.objectKey(),
                    exception
            );

            throw exception;
        }

        try {

            storagePort.delete(
                    mediaFile.objectKey()
            );

        } catch (RuntimeException exception) {

            log.error(
                    "CRITICAL: media metadata was deleted but physical media deletion failed. " +
                            "Manual reconciliation may be required. mediaId={}, objectKey={}",
                    mediaId,
                    mediaFile.objectKey(),
                    exception
            );

            throw exception;
        }
    }

    private MediaDeliveryResult uploadMedia(
            String ownerId,
            MultipartFile file,
            MediaType mediaType,
            MediaVisibility visibility
    ) {

        validateOwner(ownerId);

        validateFile(file);

        validateMediaType(
                file,
                mediaType
        );

        String mediaId =
                UUID.randomUUID().toString();

        String contentType =
                file.getContentType();

        String extension =
                resolveExtension(
                        contentType
                );

        String originalFilename =
                sanitizeOriginalFilename(
                        file.getOriginalFilename()
                );

        String objectKey =
                buildObjectKey(
                        mediaType,
                        ownerId,
                        mediaId,
                        extension
                );

        MediaFile mediaFile =
                new MediaFile(
                        mediaId,
                        objectKey,
                        originalFilename,
                        contentType,
                        file.getSize(),
                        mediaType,
                        visibility,
                        ownerId,
                        Instant.now()
                );

        boolean storageCompleted =
                false;

        try (InputStream inputStream =
                     file.getInputStream()) {

            StorageObject storageObject =
                    new StorageObject(
                            mediaId,
                            ownerId,
                            objectKey,
                            mediaType,
                            visibility,
                            originalFilename,
                            contentType,
                            extension,
                            file.getSize(),
                            inputStream
                    );

            storagePort.store(
                    storageObject
            );

            storageCompleted = true;

            try {

                MediaFile savedMedia =
                        mediaMetadataPort.save(
                                mediaFile
                        );

                String deliveryUrl =
                        buildDeliveryUrl(
                                savedMedia
                        );

                DeliveryType deliveryType =
                        savedMedia.visibility()
                                == MediaVisibility.PUBLIC
                                ? DeliveryType.URL
                                : DeliveryType.STREAM;

                return new MediaDeliveryResult(
                        savedMedia,
                        deliveryType,
                        deliveryUrl
                );

            } catch (RuntimeException exception) {

                if (storageCompleted) {

                    try {

                        storagePort.delete(
                                objectKey
                        );

                    } catch (RuntimeException cleanupException) {

                        log.error(
                                "CRITICAL: storage compensation failed. " +
                                        "Orphaned media may remain. mediaId={}, objectKey={}",
                                mediaId,
                                objectKey,
                                cleanupException
                        );
                    }
                }

                throw exception;
            }

        } catch (IOException exception) {

            throw new InvalidMediaFileException(
                    "Unable to read uploaded file"
            );
        }
    }

    private MediaFile findMediaById(
            String mediaId
    ) {

        if (mediaId == null
                || mediaId.isBlank()) {

            throw new InvalidMediaFileException(
                    "Media ID is required"
            );
        }

        return mediaMetadataPort
                .findById(mediaId)
                .orElseThrow(() ->
                        new MediaNotFoundException(
                                "Media not found"
                        )
                );
    }

    private void authorizeMediaAccess(
            MediaFile mediaFile
    ) {

        if (mediaFile.visibility()
                == MediaVisibility.PUBLIC) {

            return;
        }

        String currentUserId =
                currentUserPort.getCurrentUserId();

        validateOwner(currentUserId);

        if (!mediaFile.ownerId()
                .equals(currentUserId)) {

            throw new MediaAccessDeniedException(
                    "You are not allowed to access this media"
            );
        }
    }

    private String buildDeliveryUrl(
            MediaFile mediaFile
    ) {

        if (mediaFile.visibility()
                == MediaVisibility.PRIVATE) {

            return null;
        }

        return "/api/v1/media/"
                + mediaFile.id()
                + "/content";
    }

    private void validateOwner(
            String ownerId
    ) {

        validateOwnerId(
                ownerId
        );
    }

    private void validateOwnerId(
            String ownerId
    ) {

        if (ownerId == null
                || ownerId.isBlank()) {

            throw new InvalidMediaFileException(
                    "Authenticated media owner is required"
            );
        }
    }

    private void validateFile(
            MultipartFile file
    ) {

        if (file == null) {

            throw new InvalidMediaFileException(
                    "File is required"
            );
        }

        if (file.isEmpty()) {

            throw new InvalidMediaFileException(
                    "File must not be empty"
            );
        }

        if (file.getSize() <= 0) {

            throw new InvalidMediaFileException(
                    "File must contain data"
            );
        }

        if (file.getContentType() == null
                || file.getContentType().isBlank()) {

            throw new UnsupportedMediaTypeException(
                    "File content type is required"
            );
        }
    }

    private void validateMediaType(
            MultipartFile file,
            MediaType mediaType
    ) {

        long maxSizeBytes =
                resolveMaxSize(
                        mediaType
                );

        if (file.getSize()
                > maxSizeBytes) {

            throw new MediaTooLargeException(
                    "File exceeds the maximum allowed size"
            );
        }

        List<String> allowedContentTypes =
                resolveAllowedContentTypes(
                        mediaType
                );

        if (!allowedContentTypes.contains(
                file.getContentType()
        )) {

            throw new UnsupportedMediaTypeException(
                    "Unsupported media type"
            );
        }
    }

    private long resolveMaxSize(
            MediaType mediaType
    ) {

        return switch (mediaType) {

            case PROFILE_PHOTO ->
                    storageProperties
                            .getProfilePhoto()
                            .getMaxSizeBytes();

            case PORTFOLIO_IMAGE ->
                    storageProperties
                            .getPortfolioImage()
                            .getMaxSizeBytes();

            case DOCUMENT ->
                    storageProperties
                            .getDocument()
                            .getMaxSizeBytes();
        };
    }

    private List<String> resolveAllowedContentTypes(
            MediaType mediaType
    ) {

        return switch (mediaType) {

            case PROFILE_PHOTO ->
                    storageProperties
                            .getProfilePhoto()
                            .getAllowedContentTypes();

            case PORTFOLIO_IMAGE ->
                    storageProperties
                            .getPortfolioImage()
                            .getAllowedContentTypes();

            case DOCUMENT ->
                    storageProperties
                            .getDocument()
                            .getAllowedContentTypes();
        };
    }

    private String resolveExtension(
            String contentType
    ) {

        return switch (contentType) {

            case "image/jpeg" -> "jpg";

            case "image/png" -> "png";

            case "image/webp" -> "webp";

            case "application/pdf" -> "pdf";

            default ->
                    throw new UnsupportedMediaTypeException(
                            "Unsupported media type"
                    );
        };
    }

    private String sanitizeOriginalFilename(
            String originalFilename
    ) {

        if (originalFilename == null
                || originalFilename.isBlank()) {

            return "unknown";
        }

        String filename =
                originalFilename
                        .replace("\\", "/");

        int lastSlash =
                filename.lastIndexOf("/");

        if (lastSlash >= 0) {

            filename =
                    filename.substring(
                            lastSlash + 1
                    );
        }

        return filename;
    }

    private String buildObjectKey(
            MediaType mediaType,
            String ownerId,
            String mediaId,
            String extension
    ) {

        String prefix =
                switch (mediaType) {

                    case PROFILE_PHOTO ->
                            storageProperties
                                    .getS3()
                                    .getProfilePhotosPrefix();

                    case PORTFOLIO_IMAGE ->
                            storageProperties
                                    .getS3()
                                    .getPortfolioImagesPrefix();

                    case DOCUMENT ->
                            storageProperties
                                    .getS3()
                                    .getDocumentsPrefix();
                };

        if (prefix == null
                || prefix.isBlank()) {

            throw new StorageException(
                    "Storage prefix is not configured for media type: "
                            + mediaType
            );
        }

        return String.join(
                "/",
                prefix,
                ownerId,
                mediaId + "." + extension
        );
    }

    private int normalizePageSize(
            int size
    ) {

        if (size <= 0) {

            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(
                size,
                MAX_PAGE_SIZE
        );
    }
}