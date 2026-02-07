package org.app.courseapp.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.app.courseapp.dto.response.UploadUrlResponse;
import org.app.courseapp.dto.VideoDto;
import org.app.courseapp.model.Video;
import org.app.courseapp.repository.VideoRepository;
import org.app.courseapp.service.VideoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final MinioClient minioClient;
    private final VideoRepository videoRepository;

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<VideoDto>> getVideosByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(videoService.getVideosByLesson(lessonId, null));
    }

    @PostMapping("/lesson/{lessonId}/homework/upload-url")
    public ResponseEntity<UploadUrlResponse> getHomeworkUploadUrl(@PathVariable Long lessonId) {
        return ResponseEntity.ok(videoService.getHomeworkUploadUrl(lessonId));
    }

    @PostMapping("/lesson/{lessonId}/homework/confirm")
    public ResponseEntity<VideoDto> confirmHomeworkUpload(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Object> request
    ) {
        String objectKey = request.get("objectKey").toString();
        String title = request.get("title").toString();
        Long fileSize = Long.valueOf(request.get("fileSize").toString());

        return ResponseEntity.ok(
                videoService.confirmHomeworkUpload(lessonId, objectKey, title, fileSize)
        );
    }

    @PutMapping("/{videoId}/progress")
    public ResponseEntity<Void> updateProgress(
            @PathVariable Long videoId,
            @RequestBody Map<String, Long> request
    ) {
        Long watchedSeconds = request.get("watchedSeconds");
        videoService.updateProgress(videoId, watchedSeconds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{videoId}/complete")
    public ResponseEntity<Void> markAsCompleted(@PathVariable Long videoId) {
        videoService.markAsCompleted(videoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long videoId) {
        videoService.deleteVideo(videoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/videos/{id}/stream")
    public ResponseEntity<byte[]> streamVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String range
    ) throws Exception {

        Video video = videoRepository.findById(id).orElseThrow();

        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(video.getBucketName())
                        .object(video.getObjectKey())
                        .build()
        );

        long fileSize = stat.size();
        long start = 0;
        long end = fileSize - 1;

        if (range != null && range.startsWith("bytes=")) {
            String[] parts = range.replace("bytes=", "").split("-");
            start = Long.parseLong(parts[0]);
            if (parts.length > 1 && !parts[1].isEmpty()) {
                end = Long.parseLong(parts[1]);
            }
        }

        long contentLength = end - start + 1;

        InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(video.getBucketName())
                        .object(video.getObjectKey())
                        .offset(start)
                        .length(contentLength)
                        .build()
        );

        byte[] data = inputStream.readNBytes((int) contentLength);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(video.getContentType()));
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.set(HttpHeaders.CONTENT_RANGE,
                "bytes " + start + "-" + end + "/" + fileSize);
        headers.setContentLength(contentLength);

        return new ResponseEntity<>(data, headers, HttpStatus.PARTIAL_CONTENT);
    }
}