package com.pm.mediastorage.infrastructure.storage;

import com.pm.mediastorage.config.StorageProperties;
import com.pm.mediastorage.exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@ConditionalOnProperty(
        prefix = "storage",
        name = "provider",
        havingValue = "s3"
)
public class S3BucketInitializer {

    private final S3Client s3Client;

    private final StorageProperties storageProperties;

    public S3BucketInitializer(
            S3Client s3Client,
            StorageProperties storageProperties
    ) {
        this.s3Client = s3Client;
        this.storageProperties = storageProperties;
    }

    @PostConstruct
    public void initializeBucket() {

        StorageProperties.S3 s3Properties =
                storageProperties.getS3();

        /*
         * Bucket initialization is disabled by default.
         *
         * This is appropriate for AWS S3 because the
         * bucket is normally created and managed outside
         * the application.
         *
         * Enable this property when using this initializer
         * for development, testing, or studying.
         */
        if (!s3Properties.isInitializeBucket()) {

            return;
        }

        String bucket =
                s3Properties.getBucket();

        if (bucket == null
                || bucket.isBlank()) {

            throw new StorageException(
                    "S3 bucket must be configured"
            );
        }

        if (bucketExists(bucket)) {

            return;
        }

        if (!s3Properties.isAutoCreateBucket()) {

            throw new StorageException(
                    "S3 bucket does not exist: "
                            + bucket
            );
        }

        createBucket(bucket);
    }

    private boolean bucketExists(
            String bucket
    ) {

        try {

            HeadBucketRequest request =
                    HeadBucketRequest.builder()
                            .bucket(bucket)
                            .build();

            s3Client.headBucket(request);

            return true;

        } catch (NoSuchBucketException exception) {

            return false;

        } catch (S3Exception exception) {

            if (exception.statusCode() == 404) {

                return false;
            }

            throw new StorageException(
                    "Failed to check S3 bucket: "
                            + bucket,
                    exception
            );
        }
    }

    private void createBucket(
            String bucket
    ) {

        try {

            CreateBucketRequest.Builder
                    requestBuilder =
                    CreateBucketRequest.builder()
                            .bucket(bucket);

            String region =
                    storageProperties
                            .getS3()
                            .getRegion();

            boolean isAwsUsEast1 =
                    region != null
                            && region.equalsIgnoreCase(
                            "us-east-1"
                    );

            boolean hasEndpoint =
                    storageProperties
                            .getS3()
                            .getEndpoint() != null
                            && !storageProperties
                            .getS3()
                            .getEndpoint()
                            .isBlank();

            /*
             * MinIO and other S3-compatible providers
             * can create the bucket without an AWS
             * location constraint.
             *
             * For real AWS S3, regions other than
             * us-east-1 require the location constraint.
             */
            if (!hasEndpoint
                    && !isAwsUsEast1) {

                requestBuilder
                        .createBucketConfiguration(
                                CreateBucketConfiguration
                                        .builder()
                                        .locationConstraint(
                                                region
                                        )
                                        .build()
                        );
            }

            s3Client.createBucket(
                    requestBuilder.build()
            );

        } catch (S3Exception exception) {

            throw new StorageException(
                    "Failed to create S3 bucket: "
                            + bucket,
                    exception
            );
        }
    }
}