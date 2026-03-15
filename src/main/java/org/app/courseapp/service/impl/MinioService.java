package org.app.courseapp.service.impl;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.config.minio.MinioBucket;
import org.app.courseapp.config.minio.MinioProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
            createBucketIfNotExists(minioProperties.getVideoBucket());
            createBucketIfNotExists(minioProperties.getAvatarBucket());
            createBucketIfNotExists(minioProperties.getDocumentBucket());
            log.info("MinIO initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize MinIO", e);
        }
    }

    // ─── Resolve bucket name by enum ──────────────────────────────────────────

    private String resolveBucket(MinioBucket bucket) {
        return switch (bucket) {
            case VIDEO    -> minioProperties.getVideoBucket();
            case AVATAR   -> minioProperties.getAvatarBucket();
            case DOCUMENT -> minioProperties.getDocumentBucket();
        };
    }

    // ─── Core operations ──────────────────────────────────────────────────────

    public String getPresignedUrl(MinioBucket bucket, String objectKey, int expiryHours) {
        try {
            return publicMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(resolveBucket(bucket))
                            .object(objectKey)
                            .expiry(expiryHours, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", objectKey, e);
            throw new RuntimeException("Failed to generate URL", e);
        }
    }

    public void uploadFile(MinioBucket bucket, String objectKey, InputStream inputStream,
                           String contentType, long size) {
        String safeKey = sanitizeObjectKey(objectKey);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(resolveBucket(bucket))
                            .object(safeKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("Uploaded file: {}", safeKey);
        } catch (Exception e) {
            log.error("Failed to upload file: {}", objectKey, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }
    public void uploadVideo(MinioBucket bucket, String objectKey, InputStream inputStream) {
        Path tempInput = null;
        Path compressed = null;

        try {
            tempInput = Files.createTempFile("upload_", ".mp4");
            Files.copy(inputStream, tempInput, StandardCopyOption.REPLACE_EXISTING);

            compressed = compressVideo(tempInput);

            String safeKey = sanitizeObjectKey(objectKey);
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(resolveBucket(bucket))
                            .object(safeKey)
                            .filename(compressed.toString())
                            .contentType("video/mp4")
                            .build()
            );
            log.info("Uploaded compressed video: {}", safeKey);

        } catch (Exception e) {
            log.error("Failed to upload video: {}", objectKey, e);
            throw new RuntimeException("Failed to upload video", e);
        } finally {
            deleteTempFile(tempInput);
            deleteTempFile(compressed);
        }
    }
    private Path compressVideo(Path inputPath) throws IOException, InterruptedException {
        Path outputPath = Files.createTempFile("compressed_", ".mp4");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", inputPath.toString(),
                "-vcodec", "libx264",
                "-crf", "23",
                "-preset", "slow",
                "-acodec", "aac",
                "-b:a", "128k",
                "-movflags", "+faststart",
                "-y",
                outputPath.toString()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(line -> log.debug("FFmpeg: {}", line));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            Files.deleteIfExists(outputPath);
            throw new RuntimeException("FFmpeg failed with exit code: " + exitCode);
        }

        log.info("Video compressed: {} MB → {} MB",
                Files.size(inputPath) / 1_048_576,
                Files.size(outputPath) / 1_048_576);

        return outputPath;
    }

    private void deleteTempFile(Path path) {
        try {
            if (path != null) Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete temp file: {}", path, e);
        }
    }
    public void deleteFile(MinioBucket bucket, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(resolveBucket(bucket))
                            .object(objectKey)
                            .build()
            );
            log.info("Deleted file: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", objectKey, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    // ─── Key generators ───────────────────────────────────────────────────────

    public String generateLessonVideoKey(Long courseId, Long lessonId,
                                         String categoryName, String originalFilename) {
        String path = categoryName != null ? transliterate(categoryName) : "lesson";
        String rawKey = String.format("courses/%d/lessons/%d/%s/%d.%s",
                courseId, lessonId, path, System.currentTimeMillis(),
                getFileExtension(originalFilename));
        return sanitizeObjectKey(rawKey);
    }

    public String generateHomeworkKey(Long courseId, Long lessonId,
                                      Long userId, String originalFilename) {
        String rawKey = String.format("courses/%d/lessons/%d/homework/user_%d_%d.%s",
                courseId, lessonId, userId, System.currentTimeMillis(),
                getFileExtension(originalFilename));
        return sanitizeObjectKey(rawKey);
    }
    public String generateCertificateKey(Long specialistId, Long certId, String originalFilename) {
        String rawKey = String.format("specialists/%d/certificates/%d_%d.%s",
                specialistId, certId, System.currentTimeMillis(),
                getFileExtension(originalFilename));
        return sanitizeObjectKey(rawKey);
    }
    public String generateAvatarKey(Long userId, String originalFilename) {
        String rawKey = String.format("avatars/user_%d_%d.%s",
                userId, System.currentTimeMillis(),
                getFileExtension(originalFilename));
        return sanitizeObjectKey(rawKey);
    }

    private void createBucketIfNotExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Created bucket: {}", bucketName);
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

    private String getFileExtension(String filename) {
        if (filename == null) return "bin";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "bin";
    }
}