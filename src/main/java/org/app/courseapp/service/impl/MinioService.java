package org.app.courseapp.service.impl;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.config.minio.MinioProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MinioService {
    private final MinioClient minioClient;
    private final MinioClient publicMinioClient;
    private final MinioProperties minioProperties;

    public MinioService(
            MinioClient minioClient,
            @Qualifier("publicMinioClient") MinioClient publicMinioClient,
            MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.publicMinioClient = publicMinioClient;
        this.minioProperties = minioProperties;
    }

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
            return publicMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .expiry(expiryHours, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", objectKey, e);
            throw new RuntimeException("Failed to generate URL", e);
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
    private String transliterate(String text) {
        if (text == null) return "";
        String lower = text.toLowerCase();
        return lower
                .replace("а", "a").replace("б", "b").replace("в", "v")
                .replace("г", "g").replace("д", "d").replace("е", "e")
                .replace("ё", "yo").replace("ж", "zh").replace("з", "z")
                .replace("и", "i").replace("й", "y").replace("к", "k")
                .replace("л", "l").replace("м", "m").replace("н", "n")
                .replace("о", "o").replace("п", "p").replace("р", "r")
                .replace("с", "s").replace("т", "t").replace("у", "u")
                .replace("ф", "f").replace("х", "kh").replace("ц", "ts")
                .replace("ч", "ch").replace("ш", "sh").replace("щ", "sch")
                .replace("ъ", "").replace("ы", "y").replace("ь", "")
                .replace("э", "e").replace("ю", "yu").replace("я", "ya")
                .replace(" ", "_");
    }
    public String sanitizeObjectKey(String key) {
        String transliterated = transliterate(key);
        String normalized = Normalizer.normalize(transliterated, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replaceAll("[^a-zA-Z0-9._\\-/]", "_");
        return normalized;
    }
    public String generateLessonVideoKey(Long courseId, Long lessonId, String categoryName, String originalFilename) {
        log.info("categoryName raw: '{}'", categoryName);
        String path = categoryName != null ? transliterate(categoryName) : "lesson";
        log.info("categoryName after transliterate: '{}'", path);
        String rawKey = String.format("courses/%d/lessons/%d/%s/%d.%s",
                courseId, lessonId, path, System.currentTimeMillis(), getFileExtension(originalFilename));
        return sanitizeObjectKey(rawKey);
    }
    public String generateHomeworkKey(Long courseId, Long lessonId, Long userId, String originalFilename) {
        String rawKey = String.format("courses/%d/lessons/%d/homework/user_%d_%d.%s",
                courseId, lessonId, userId, System.currentTimeMillis(), getFileExtension(originalFilename));
        return sanitizeObjectKey(rawKey);
    }
    private String getFileExtension(String filename) {
        if (filename == null) return "mp4";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "mp4";
    }
}