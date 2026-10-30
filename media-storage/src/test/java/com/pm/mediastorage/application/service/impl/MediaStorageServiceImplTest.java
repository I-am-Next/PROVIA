package com.pm.mediastorage.application.service.impl;

import com.pm.mediastorage.config.StorageProperties;
import com.pm.mediastorage.domain.model.MediaFile;
import com.pm.mediastorage.domain.model.MediaType;
import com.pm.mediastorage.domain.model.MediaVisibility;
import com.pm.mediastorage.domain.port.out.CurrentUserPort;
import com.pm.mediastorage.domain.port.out.MediaMetadataPort;
import com.pm.mediastorage.domain.port.out.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaStorageServiceImplTest {

    @Mock
    private StoragePort storagePort;

    @Mock
    private MediaMetadataPort mediaMetadataPort;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private StorageProperties storageProperties;

    private MediaStorageServiceImpl mediaStorageService;

    private String mediaId;
    private String ownerId;
    private String objectKey;

    @BeforeEach
    void setUp() {

        mediaStorageService =
                new MediaStorageServiceImpl(
                        storagePort,
                        mediaMetadataPort,
                        currentUserPort,
                        storageProperties
                );

        mediaId = "media-123";
        ownerId = "user-123";
        objectKey = "portfolio/user-123/media-123.jpg";
    }

    @Test
    void shouldDeleteMetadataBeforePhysicalStorage() {

        MediaFile mediaFile =
                createMediaFile();

        when(currentUserPort.getCurrentUserId())
                .thenReturn(ownerId);

        when(mediaMetadataPort.findById(mediaId))
                .thenReturn(Optional.of(mediaFile));

        mediaStorageService.deleteMedia(mediaId);

        InOrder inOrder =
                inOrder(
                        mediaMetadataPort,
                        storagePort
                );

        inOrder.verify(mediaMetadataPort)
                .deleteById(mediaId);

        inOrder.verify(storagePort)
                .delete(objectKey);
    }

    @Test
    void shouldNotDeletePhysicalStorageWhenMetadataDeletionFails() {

        MediaFile mediaFile =
                createMediaFile();

        RuntimeException databaseException =
                new RuntimeException(
                        "Database deletion failed"
                );

        when(currentUserPort.getCurrentUserId())
                .thenReturn(ownerId);

        when(mediaMetadataPort.findById(mediaId))
                .thenReturn(Optional.of(mediaFile));

        doThrow(databaseException)
                .when(mediaMetadataPort)
                .deleteById(mediaId);

        assertThrows(
                RuntimeException.class,
                () -> mediaStorageService.deleteMedia(mediaId)
        );

        verify(mediaMetadataPort)
                .deleteById(mediaId);

        verify(
                storagePort,
                never()
        ).delete(objectKey);
    }

    @Test
    void shouldPropagateStorageDeletionFailureAfterMetadataDeletion() {

        MediaFile mediaFile =
                createMediaFile();

        RuntimeException storageException =
                new RuntimeException(
                        "S3 deletion failed"
                );

        when(currentUserPort.getCurrentUserId())
                .thenReturn(ownerId);

        when(mediaMetadataPort.findById(mediaId))
                .thenReturn(Optional.of(mediaFile));

        doThrow(storageException)
                .when(storagePort)
                .delete(objectKey);

        assertThrows(
                RuntimeException.class,
                () -> mediaStorageService.deleteMedia(mediaId)
        );

        InOrder inOrder =
                inOrder(
                        mediaMetadataPort,
                        storagePort
                );

        inOrder.verify(mediaMetadataPort)
                .deleteById(mediaId);

        inOrder.verify(storagePort)
                .delete(objectKey);
    }

    @Test
    void shouldNotDeleteMediaOwnedByAnotherUser() {

        MediaFile mediaFile =
                createMediaFile();

        String anotherUserId =
                "another-user";

        when(currentUserPort.getCurrentUserId())
                .thenReturn(anotherUserId);

        when(mediaMetadataPort.findById(mediaId))
                .thenReturn(Optional.of(mediaFile));

        assertThrows(
                RuntimeException.class,
                () -> mediaStorageService.deleteMedia(mediaId)
        );

        verify(
                mediaMetadataPort,
                never()
        ).deleteById(mediaId);

        verify(
                storagePort,
                never()
        ).delete(objectKey);
    }

    private MediaFile createMediaFile() {

        return new MediaFile(
                mediaId,
                objectKey,
                "portfolio.jpg",
                "image/jpeg",
                1024L,
                MediaType.PORTFOLIO_IMAGE,
                MediaVisibility.PUBLIC,
                ownerId,
                java.time.Instant.now()
        );
    }
}