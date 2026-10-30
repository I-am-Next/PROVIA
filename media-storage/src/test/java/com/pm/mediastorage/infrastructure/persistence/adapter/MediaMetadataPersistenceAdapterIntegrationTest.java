package com.pm.mediastorage.infrastructure.persistence.adapter;

import com.pm.mediastorage.domain.model.MediaFile;
import com.pm.mediastorage.domain.model.MediaType;
import com.pm.mediastorage.domain.model.MediaVisibility;
import com.pm.mediastorage.infrastructure.persistence.entity.MediaFileEntity;
import com.pm.mediastorage.infrastructure.persistence.repository.MediaFileJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(MediaMetadataPersistenceAdapter.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@TestPropertySource(
        properties = {
                "spring.datasource.url=jdbc:postgresql://localhost:5443/media_db",
                "spring.datasource.username=media_user",
                "spring.datasource.password=media_pass",
                "spring.datasource.driver-class-name=org.postgresql.Driver",
                "spring.jpa.hibernate.ddl-auto=validate"
        }
)
class MediaMetadataPersistenceAdapterIntegrationTest {

    @Autowired
    private MediaMetadataPersistenceAdapter adapter;

    @Autowired
    private MediaFileJpaRepository repository;

    @AfterEach
    void cleanUp() {

        repository.deleteAll();
    }

    @Test
    void shouldSaveMediaMetadataToPostgreSQL() {

        MediaFile mediaFile =
                createMediaFile(
                        "integration-save-001"
                );

        MediaFile saved =
                adapter.save(
                        mediaFile
                );

        assertNotNull(saved);

        assertEquals(
                mediaFile.id(),
                saved.id()
        );

        assertEquals(
                mediaFile.objectKey(),
                saved.objectKey()
        );

        assertEquals(
                mediaFile.ownerId(),
                saved.ownerId()
        );

        assertEquals(
                MediaType.PORTFOLIO_IMAGE,
                saved.mediaType()
        );

        assertEquals(
                MediaVisibility.PUBLIC,
                saved.visibility()
        );

        Optional<MediaFileEntity> persisted =
                repository.findById(
                        mediaFile.id()
                );

        assertTrue(
                persisted.isPresent()
        );

        assertEquals(
                mediaFile.objectKey(),
                persisted.get().getObjectKey()
        );
    }

    @Test
    void shouldDeleteMediaMetadataAndCommitTransaction() {

        MediaFile mediaFile =
                createMediaFile(
                        "integration-delete-001"
                );

        adapter.save(
                mediaFile
        );

        assertTrue(
                repository.existsById(
                        mediaFile.id()
                )
        );

        adapter.deleteById(
                mediaFile.id()
        );

        /*
         * The important assertion:
         *
         * deleteById() has returned, so the transaction owned by
         * MediaMetadataPersistenceAdapter should already have
         * completed successfully.
         */
        assertFalse(
                repository.existsById(
                        mediaFile.id()
                )
        );
    }

    @Test
    void shouldDeleteOnlyRequestedMediaMetadata() {

        MediaFile first =
                createMediaFile(
                        "integration-delete-first"
                );

        MediaFile second =
                createMediaFile(
                        "integration-delete-second"
                );

        adapter.save(first);
        adapter.save(second);

        adapter.deleteById(
                first.id()
        );

        assertFalse(
                repository.existsById(
                        first.id()
                )
        );

        assertTrue(
                repository.existsById(
                        second.id()
                )
        );
    }

    private MediaFile createMediaFile(
            String mediaId
    ) {

        return new MediaFile(
                mediaId,
                "portfolio/user-123/"
                        + mediaId
                        + ".jpg",
                "portfolio.jpg",
                "image/jpeg",
                1024L,
                MediaType.PORTFOLIO_IMAGE,
                MediaVisibility.PUBLIC,
                "user-123",
                Instant.now()
        );
    }
}