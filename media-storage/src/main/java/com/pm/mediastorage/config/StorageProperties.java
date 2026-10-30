package com.pm.mediastorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private String provider = "local";

    private Local local = new Local();

    private S3 s3 = new S3();

    private ProfilePhoto profilePhoto =
            new ProfilePhoto();

    private PortfolioImage portfolioImage =
            new PortfolioImage();

    private Document document =
            new Document();

    public String getProvider() {
        return provider;
    }

    public void setProvider(
            String provider
    ) {
        this.provider = provider;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(
            Local local
    ) {
        this.local = local;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(
            S3 s3
    ) {
        this.s3 = s3;
    }

    public ProfilePhoto getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(
            ProfilePhoto profilePhoto
    ) {
        this.profilePhoto = profilePhoto;
    }

    public PortfolioImage getPortfolioImage() {
        return portfolioImage;
    }

    public void setPortfolioImage(
            PortfolioImage portfolioImage
    ) {
        this.portfolioImage = portfolioImage;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(
            Document document
    ) {
        this.document = document;
    }

    public static class Local {

        private String uploadDir =
                "uploads";

        private String publicBaseUrl =
                "http://localhost:8080";

        public String getUploadDir() {
            return uploadDir;
        }

        public void setUploadDir(
                String uploadDir
        ) {
            this.uploadDir = uploadDir;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(
                String publicBaseUrl
        ) {
            this.publicBaseUrl = publicBaseUrl;
        }
    }

    public static class S3 {

        private String bucket;

        private String region =
                "us-east-1";

        private String endpoint;

        private boolean pathStyleAccessEnabled =
                false;

        private boolean autoCreateBucket =
                false;

        private boolean initializeBucket =
                false;

        private String publicBaseUrl;

        private String profilePhotosPrefix =
                "profile-photos";

        private String portfolioImagesPrefix =
                "portfolio-images";

        private String documentsPrefix =
                "documents";

        public String getBucket() {
            return bucket;
        }

        public void setBucket(
                String bucket
        ) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(
                String region
        ) {
            this.region = region;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(
                String endpoint
        ) {
            this.endpoint = endpoint;
        }

        public boolean isPathStyleAccessEnabled() {
            return pathStyleAccessEnabled;
        }

        public void setPathStyleAccessEnabled(
                boolean pathStyleAccessEnabled
        ) {
            this.pathStyleAccessEnabled =
                    pathStyleAccessEnabled;
        }

        public boolean isAutoCreateBucket() {
            return autoCreateBucket;
        }

        public void setAutoCreateBucket(
                boolean autoCreateBucket
        ) {
            this.autoCreateBucket =
                    autoCreateBucket;
        }

        public boolean isInitializeBucket() {
            return initializeBucket;
        }

        public void setInitializeBucket(
                boolean initializeBucket
        ) {
            this.initializeBucket =
                    initializeBucket;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(
                String publicBaseUrl
        ) {
            this.publicBaseUrl = publicBaseUrl;
        }

        public String getProfilePhotosPrefix() {
            return profilePhotosPrefix;
        }

        public void setProfilePhotosPrefix(
                String profilePhotosPrefix
        ) {
            this.profilePhotosPrefix =
                    profilePhotosPrefix;
        }

        public String getPortfolioImagesPrefix() {
            return portfolioImagesPrefix;
        }

        public void setPortfolioImagesPrefix(
                String portfolioImagesPrefix
        ) {
            this.portfolioImagesPrefix =
                    portfolioImagesPrefix;
        }

        public String getDocumentsPrefix() {
            return documentsPrefix;
        }

        public void setDocumentsPrefix(
                String documentsPrefix
        ) {
            this.documentsPrefix =
                    documentsPrefix;
        }
    }

    public static class ProfilePhoto {

        private long maxSizeBytes =
                5 * 1024 * 1024;

        private List<String> allowedContentTypes =
                new ArrayList<>(
                        List.of(
                                "image/jpeg",
                                "image/png",
                                "image/webp"
                        )
                );

        public long getMaxSizeBytes() {
            return maxSizeBytes;
        }

        public void setMaxSizeBytes(
                long maxSizeBytes
        ) {
            this.maxSizeBytes =
                    maxSizeBytes;
        }

        public List<String>
        getAllowedContentTypes() {
            return allowedContentTypes;
        }

        public void setAllowedContentTypes(
                List<String> allowedContentTypes
        ) {
            this.allowedContentTypes =
                    allowedContentTypes;
        }
    }

    public static class PortfolioImage {

        private long maxSizeBytes =
                10 * 1024 * 1024;

        private List<String> allowedContentTypes =
                new ArrayList<>(
                        List.of(
                                "image/jpeg",
                                "image/png",
                                "image/webp"
                        )
                );

        public long getMaxSizeBytes() {
            return maxSizeBytes;
        }

        public void setMaxSizeBytes(
                long maxSizeBytes
        ) {
            this.maxSizeBytes =
                    maxSizeBytes;
        }

        public List<String>
        getAllowedContentTypes() {
            return allowedContentTypes;
        }

        public void setAllowedContentTypes(
                List<String> allowedContentTypes
        ) {
            this.allowedContentTypes =
                    allowedContentTypes;
        }
    }

    public static class Document {

        private long maxSizeBytes =
                20 * 1024 * 1024;

        private List<String>
                allowedContentTypes =
                new ArrayList<>(
                        List.of(
                                "application/pdf"
                        )
                );

        public long getMaxSizeBytes() {
            return maxSizeBytes;
        }

        public void setMaxSizeBytes(
                long maxSizeBytes
        ) {
            this.maxSizeBytes =
                    maxSizeBytes;
        }

        public List<String>
        getAllowedContentTypes() {
            return allowedContentTypes;
        }

        public void setAllowedContentTypes(
                List<String> allowedContentTypes
        ) {
            this.allowedContentTypes =
                    allowedContentTypes;
        }
    }
}