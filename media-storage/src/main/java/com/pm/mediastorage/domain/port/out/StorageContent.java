package com.pm.mediastorage.domain.port.out;

import java.io.InputStream;

public record StorageContent(
        InputStream content,
        String contentType,
        long contentLength
) {
}