package com.pm.mediastorage.infrastructure.persistence.entity;

import com.pm.mediastorage.domain.model.MediaType;
import com.pm.mediastorage.domain.model.MediaVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "media_files")
public class MediaFileEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(
            name = "object_key",
            nullable = false,
            unique = true,
            length = 500
    )
    private String objectKey;

    @Column(
            name = "original_filename",
            nullable = false,
            length = 255
    )
    private String originalFilename;

    @Column(
            name = "content_type",
            nullable = false,
            length = 100
    )
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "media_type",
            nullable = false,
            length = 50
    )
    private MediaType mediaType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "visibility",
            nullable = false,
            length = 20
    )
    private MediaVisibility visibility;

    @Column(
            name = "owner_id",
            nullable = false,
            length = 100
    )
    private String ownerId;

    @Column(
            name = "uploaded_at",
            nullable = false
    )
    private Instant uploadedAt;

    protected MediaFileEntity() {
    }

    public MediaFileEntity(
            String id,
            String objectKey,
            String originalFilename,
            String contentType,
            long size,
            MediaType mediaType,
            MediaVisibility visibility,
            String ownerId,
            Instant uploadedAt
    ) {
        this.id = id;
        this.objectKey = objectKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.mediaType = mediaType;
        this.visibility = visibility;
        this.ownerId = ownerId;
        this.uploadedAt = uploadedAt;
    }

    public String getId() {
        return id;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public MediaVisibility getVisibility() {
        return visibility;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}