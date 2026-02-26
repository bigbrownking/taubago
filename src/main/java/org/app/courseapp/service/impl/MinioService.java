package org.app.courseapp.service.impl;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.config.minio.MinioProperties;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void init() {
        try {
            createBucketIfNotExists();
            log.info("MinIO initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize MinIO", e);
        }
    }

    public String getPresignedUrl(String objectKey, int expiryHours) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .expiry(expiryHours, TimeUnit.HOURS)
                            .build()
            );

            return swapHost(url, minioProperties.getPublicUrl());

        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", objectKey, e);
            throw new RuntimeException("Failed to generate URL", e);
        }
    }

    private String swapHost(String originalUrl, String publicBaseUrl) {
        try {
            URI original = new URI(originalUrl);
            URI publicBase = new URI(publicBaseUrl);

            // Build the authority (host:port) from publicBase
            String newAuthority = publicBase.getHost();
            if (publicBase.getPort() != -1) {
                newAuthority += ":" + publicBase.getPort();
            }

            String oldAuthority = original.getHost();
            if (original.getPort() != -1) {
                oldAuthority += ":" + original.getPort();
            }

            // Simple string replacement of scheme://host:port only — never touch the query
            String originalAuthority = original.getScheme() + "://" + oldAuthority;
            String newBase = publicBase.getScheme() + "://" + newAuthority;

            return originalUrl.replace(originalAuthority, newBase);

        } catch (URISyntaxException e) {
            log.warn("Could not rewrite MinIO URL, returning original: {}", e.getMessage());
            return originalUrl;
        }
    }

    private void createBucketIfNotExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .build()
        );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .build()
            );
            log.info("Created bucket: {}", minioProperties.getBucket());
        }
    }

    public void uploadFile(String objectKey, InputStream inputStream, String contentType, long size) {
        String safeKey = sanitizeObjectKey(objectKey);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(safeKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("Uploaded file: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to upload file: {}", objectKey, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .build()
            );
            log.info("Deleted file: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", objectKey, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }
    public String sanitizeObjectKey(String key) {
        String normalized = Normalizer.normalize(key, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replaceAll("[^a-zA-Z0-9._\\-/]", "_");
        return normalized;
    }
}