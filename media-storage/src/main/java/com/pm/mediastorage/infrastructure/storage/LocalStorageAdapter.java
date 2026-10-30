package com.pm.mediastorage.infrastructure.storage;

import com.pm.mediastorage.config.StorageProperties;
import com.pm.mediastorage.domain.port.out.StorageContent;
import com.pm.mediastorage.domain.port.out.StorageObject;
import com.pm.mediastorage.domain.port.out.StoragePort;
import com.pm.mediastorage.exception.MediaNotFoundException;
import com.pm.mediastorage.exception.StorageException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ConditionalOnProperty(
        prefix = "storage",
        name = "provider",
        havingValue = "local",
        matchIfMissing = true
)
@Component
public class LocalStorageAdapter implements StoragePort {

    private final Path rootDirectory;

    public LocalStorageAdapter(
            StorageProperties properties
    ) {
        this.rootDirectory =
                Paths.get(
                                properties
                                        .getLocal()
                                        .getUploadDir()
                        )
                        .toAbsolutePath()
                        .normalize();

        createRootDirectory();
    }

    @Override
    public void store(
            StorageObject object
    ) {
        Path targetPath =
                resolveSafePath(
                        object.objectKey()
                );

        try {

            Files.createDirectories(
                    targetPath.getParent()
            );

            try (InputStream inputStream =
                         object.content()) {

                Files.copy(
                        inputStream,
                        targetPath
                );
            }

        } catch (IOException exception) {

            throw new StorageException(
                    "Failed to store media file",
                    exception
            );
        }
    }

    @Override
    public StorageContent retrieve(
            String objectKey
    ) {
        Path targetPath =
                resolveSafePath(objectKey);

        if (!Files.exists(targetPath)
                || !Files.isRegularFile(targetPath)) {

            throw new MediaNotFoundException(
                    "Media file not found"
            );
        }

        try {

            InputStream inputStream =
                    Files.newInputStream(targetPath);

            long contentLength =
                    Files.size(targetPath);

            String contentType =
                    Files.probeContentType(targetPath);

            return new StorageContent(
                    inputStream,
                    contentType,
                    contentLength
            );

        } catch (IOException exception) {

            throw new StorageException(
                    "Failed to retrieve media file",
                    exception
            );
        }
    }

    @Override
    public void delete(
            String objectKey
    ) {
        Path targetPath =
                resolveSafePath(objectKey);

        try {

            Files.deleteIfExists(targetPath);

        } catch (IOException exception) {

            throw new StorageException(
                    "Failed to delete media file",
                    exception
            );
        }
    }

    private Path resolveSafePath(
            String objectKey
    ) {
        Path resolvedPath =
                rootDirectory
                        .resolve(objectKey)
                        .normalize();

        if (!resolvedPath.startsWith(rootDirectory)) {

            throw new StorageException(
                    "Invalid storage object key"
            );
        }

        return resolvedPath;
    }

    private void createRootDirectory() {

        try {

            Files.createDirectories(
                    rootDirectory
            );

        } catch (IOException exception) {

            throw new StorageException(
                    "Unable to initialize local storage",
                    exception
            );
        }
    }
}