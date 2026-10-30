package com.pm.mediastorage.infrastructure.storage;

import com.pm.mediastorage.config.StorageProperties;
import com.pm.mediastorage.domain.port.out.StorageContent;
import com.pm.mediastorage.domain.port.out.StorageObject;
import com.pm.mediastorage.domain.port.out.StoragePort;
import com.pm.mediastorage.exception.MediaNotFoundException;
import com.pm.mediastorage.exception.StorageException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@ConditionalOnProperty(
        prefix = "storage",
        name = "provider",
        havingValue = "s3"
)
public class S3StorageAdapter
        implements StoragePort {

    private final S3Client s3Client;

    private final StorageProperties
            storageProperties;

    public S3StorageAdapter(
            S3Client s3Client,
            StorageProperties storageProperties
    ) {
        this.s3Client = s3Client;
        this.storageProperties =
                storageProperties;
    }

    @Override
    public void store(
            StorageObject object
    ) {

        try {

            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(
                                    storageProperties
                                            .getS3()
                                            .getBucket()
                            )
                            .key(
                                    object.objectKey()
                            )
                            .contentType(
                                    object.contentType()
                            )
                            .contentLength(
                                    object.size()
                            )
                            .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            object.content(),
                            object.size()
                    )
            );

        } catch (S3Exception exception) {

            throw new StorageException(
                    "Failed to store media in S3",
                    exception
            );
        }
    }

    @Override
    public StorageContent retrieve(
            String objectKey
    ) {

        try {

            GetObjectRequest request =
                    GetObjectRequest.builder()
                            .bucket(
                                    storageProperties
                                            .getS3()
                                            .getBucket()
                            )
                            .key(
                                    objectKey
                            )
                            .build();

            ResponseInputStream<
                    GetObjectResponse
                    > response =
                    s3Client.getObject(
                            request
                    );

            GetObjectResponse metadata =
                    response.response();

            return new StorageContent(
                    response,
                    metadata.contentType(),
                    metadata.contentLength()
            );

        } catch (NoSuchKeyException exception) {

            throw new MediaNotFoundException(
                    "Media file not found"
            );

        } catch (S3Exception exception) {

            if (exception.statusCode() == 404) {

                throw new MediaNotFoundException(
                        "Media file not found"
                );
            }

            throw new StorageException(
                    "Failed to retrieve media from S3",
                    exception
            );
        }
    }

    @Override
    public void delete(
            String objectKey
    ) {

        try {

            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(
                                    storageProperties
                                            .getS3()
                                            .getBucket()
                            )
                            .key(
                                    objectKey
                            )
                            .build();

            s3Client.deleteObject(
                    request
            );

        } catch (S3Exception exception) {

            throw new StorageException(
                    "Failed to delete media from S3",
                    exception
            );
        }
    }
}