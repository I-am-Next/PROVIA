package com.pm.mediastorage.api.dto.response;

import com.pm.mediastorage.domain.model.MediaPage;

import java.util.List;

public record MediaGalleryResponse(

        List<PublicMediaResponse> content,

        int page,

        int size,

        long totalElements,

        int totalPages

) {

    public static MediaGalleryResponse from(
            MediaPage mediaPage
    ) {

        return new MediaGalleryResponse(
                mediaPage.content()
                        .stream()
                        .map(
                                PublicMediaResponse::from
                        )
                        .toList(),
                mediaPage.page(),
                mediaPage.size(),
                mediaPage.totalElements(),
                mediaPage.totalPages()
        );
    }
}