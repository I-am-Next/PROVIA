package com.pm.mediastorage.infrastructure.persistence.adapter;

import com.pm.mediastorage.domain.model.MediaFile;
import com.pm.mediastorage.domain.model.MediaPage;
import com.pm.mediastorage.domain.model.MediaType;
import com.pm.mediastorage.domain.model.MediaVisibility;
import com.pm.mediastorage.domain.port.out.MediaMetadataPort;
import com.pm.mediastorage.infrastructure.persistence.entity.MediaFileEntity;
import com.pm.mediastorage.infrastructure.persistence.repository.MediaFileJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class MediaMetadataPersistenceAdapter
        implements MediaMetadataPort {

    private final MediaFileJpaRepository repository;

    public MediaMetadataPersistenceAdapter(
            MediaFileJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public MediaFile save(
            MediaFile mediaFile
    ) {

        MediaFileEntity entity =
                new MediaFileEntity(
                        mediaFile.id(),
                        mediaFile.objectKey(),
                        mediaFile.originalFilename(),
                        mediaFile.contentType(),
                        mediaFile.size(),
                        mediaFile.mediaType(),
                        mediaFile.visibility(),
                        mediaFile.ownerId(),
                        mediaFile.uploadedAt()
                );

        MediaFileEntity saved =
                repository.save(entity);

        return toDomain(saved);
    }

    @Override
    public Optional<MediaFile> findById(
            String mediaId
    ) {

        return repository.findById(mediaId)
                .map(this::toDomain);
    }

    @Override
    public MediaPage findPublicMediaByOwnerIdAndType(
            String ownerId,
            MediaType mediaType,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );

        Page<MediaFileEntity> result =
                repository
                        .findByOwnerIdAndMediaTypeAndVisibilityOrderByUploadedAtDesc(
                                ownerId,
                                mediaType,
                                MediaVisibility.PUBLIC,
                                pageable
                        );

        return new MediaPage(
                result.getContent()
                        .stream()
                        .map(this::toDomain)
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void deleteById(
            String mediaId
    ) {

        repository.deleteById(
                mediaId
        );
    }

    private MediaFile toDomain(
            MediaFileEntity entity
    ) {

        return new MediaFile(
                entity.getId(),
                entity.getObjectKey(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getSize(),
                entity.getMediaType(),
                entity.getVisibility(),
                entity.getOwnerId(),
                entity.getUploadedAt()
        );
    }
}