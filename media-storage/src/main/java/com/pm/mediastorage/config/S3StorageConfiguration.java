package com.pm.mediastorage.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Configuration
@ConditionalOnProperty(
        prefix = "storage",
        name = "provider",
        havingValue = "s3"
)
public class S3StorageConfiguration {

    @Bean
    public S3Client s3Client(
            StorageProperties storageProperties
    ) {

        StorageProperties.S3 s3Properties =
                storageProperties.getS3();

        S3ClientBuilder builder =
                S3Client.builder()
                        .region(
                                Region.of(
                                        s3Properties.getRegion()
                                )
                        )
                        .serviceConfiguration(
                                S3Configuration.builder()
                                        .pathStyleAccessEnabled(
                                                s3Properties
                                                        .isPathStyleAccessEnabled()
                                        )
                                        .build()
                        )
                        .credentialsProvider(
                                DefaultCredentialsProvider.create()
                        );

        configureEndpoint(
                builder,
                s3Properties
        );

        return builder.build();
    }

    private void configureEndpoint(
            S3ClientBuilder builder,
            StorageProperties.S3 s3Properties
    ) {

        String endpoint =
                s3Properties.getEndpoint();

        if (endpoint == null
                || endpoint.isBlank()) {

            return;
        }

        builder.endpointOverride(
                URI.create(endpoint)
        );
    }
}