package com.pm.mediastorage.api.controller;

import com.pm.mediastorage.api.dto.response.ApiResponse;
import com.pm.mediastorage.api.dto.response.MediaFileResponse;
import com.pm.mediastorage.api.dto.response.MediaGalleryResponse;
import com.pm.mediastorage.application.model.MediaContentResult;
import com.pm.mediastorage.application.model.MediaDeliveryResult;
import com.pm.mediastorage.domain.model.MediaPage;
import com.pm.mediastorage.domain.port.in.MediaStorageUseCase;
import com.pm.mediastorage.domain.port.out.StorageContent;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/media")
public class MediaStorageController {

    private final MediaStorageUseCase mediaStorageUseCase;

    public MediaStorageController(
            MediaStorageUseCase mediaStorageUseCase
    ) {

        this.mediaStorageUseCase =
                mediaStorageUseCase;
    }

    @PostMapping(
            value = "/profile-photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<MediaFileResponse>>
    uploadProfilePhoto(
            @RequestPart("file") MultipartFile file
    ) {

        MediaDeliveryResult result =
                mediaStorageUseCase
                        .uploadProfilePhoto(file);

        MediaFileResponse response =
                MediaFileResponse.from(
                        result.mediaFile(),
                        result.url()
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PostMapping(
            value = "/portfolio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<MediaFileResponse>>
    uploadPortfolioImage(
            @RequestPart("file") MultipartFile file
    ) {

        MediaDeliveryResult result =
                mediaStorageUseCase
                        .uploadPortfolioImage(file);

        MediaFileResponse response =
                MediaFileResponse.from(
                        result.mediaFile(),
                        result.url()
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PostMapping(
            value = "/document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<MediaFileResponse>>
    uploadDocument(
            @RequestPart("file") MultipartFile file
    ) {

        MediaDeliveryResult result =
                mediaStorageUseCase
                        .uploadDocument(file);

        MediaFileResponse response =
                MediaFileResponse.from(
                        result.mediaFile(),
                        result.url()
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping(
            "/public/owners/{ownerId}/portfolio"
    )
    public ResponseEntity<
            ApiResponse<MediaGalleryResponse>
            >
    getPublicPortfolio(
            @PathVariable String ownerId,
            @RequestParam(
                    defaultValue = "0"
            ) int page,
            @RequestParam(
                    defaultValue = "24"
            ) int size
    ) {

        MediaPage mediaPage =
                mediaStorageUseCase
                        .getPublicPortfolio(
                                ownerId,
                                page,
                                size
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        MediaGalleryResponse.from(
                                mediaPage
                        )
                )
        );
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<ApiResponse<MediaFileResponse>>
    getMedia(
            @PathVariable String mediaId
    ) {

        MediaDeliveryResult result =
                mediaStorageUseCase
                        .getMedia(mediaId);

        MediaFileResponse response =
                MediaFileResponse.from(
                        result.mediaFile(),
                        result.url()
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping("/{mediaId}/content")
    public ResponseEntity<Resource>
    getMediaContent(
            @PathVariable String mediaId
    ) {

        MediaContentResult result =
                mediaStorageUseCase
                        .getMediaContent(mediaId);

        StorageContent content =
                result.storageContent();

        MediaType mediaType =
                resolveMediaType(
                        content.contentType()
                );

        InputStreamResource resource =
                new InputStreamResource(
                        content.content()
                );

        String filename =
                result.mediaFile()
                        .originalFilename();

        ContentDisposition contentDisposition =
                ContentDisposition
                        .inline()
                        .filename(
                                filename,
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(
                        content.contentLength()
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(resource);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<ApiResponse<Void>>
    deleteMedia(
            @PathVariable String mediaId
    ) {

        mediaStorageUseCase.deleteMedia(
                mediaId
        );

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    private MediaType resolveMediaType(
            String contentType
    ) {

        if (contentType == null
                || contentType.isBlank()) {

            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {

            return MediaType.parseMediaType(
                    contentType
            );

        } catch (IllegalArgumentException exception) {

            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}