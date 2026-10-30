package com.pm.mediastorage.domain.model;

import java.util.List;

public record MediaPage(

        List<MediaFile> content,

        int page,

        int size,

        long totalElements,

        int totalPages

) {

    public MediaPage {

        content = List.copyOf(content);

    }
}