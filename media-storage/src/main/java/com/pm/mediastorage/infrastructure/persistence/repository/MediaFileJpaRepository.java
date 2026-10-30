package com.pm.mediastorage.infrastructure.persistence.repository;

import com.pm.mediastorage.domain.model.MediaType;
import com.pm.mediastorage.infrastructure.persistence.entity.MediaFileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFileJpaRepository
        extends JpaRepository<MediaFileEntity, String> {

    Page<MediaFileEntity> findByOwnerIdAndMediaTypeAndVisibilityOrderByUploadedAtDesc(
            String ownerId,
            MediaType mediaType,
            com.pm.mediastorage.domain.model.MediaVisibility visibility,
            Pageable pageable
    );
}