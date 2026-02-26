package org.app.courseapp.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.request.UpdateProgressRequest;
import org.app.courseapp.dto.response.VideoDto;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Video Management", description = "APIs for video operations")
public class VideoController {

    private final VideoService videoService;
    private final MinioClient minioClient;
    private final VideoRepository videoRepository;

    @GetMapping("/lesson/{lessonId}")
    @Operation(summary = "Get videos by lesson", description = "Get all accessible videos for a lesson")
    public ResponseEntity<List<VideoDto>> getVideosByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(videoService.getVideosByLesson(lessonId));
    }

    @GetMapping("/{videoId}")
    @Operation(summary = "Get video by ID", description = "Get video details with access check")
    public ResponseEntity<VideoDto> getVideoById(@PathVariable Long videoId) {
        return ResponseEntity.ok(videoService.getVideoById(videoId));
    }

    @GetMapping("/{videoId}/has-access")
    @Operation(summary = "Check video access", description = "Check if current user has access to video")
    public ResponseEntity<Boolean> hasAccessToVideo(@PathVariable Long videoId) {
        return ResponseEntity.ok(videoService.hasAccessToVideo(videoId));
    }

    @PutMapping("/{videoId}/progress")
    @Operation(summary = "Update video progress", description = "Update watch progress for a video")
    public ResponseEntity<Void> updateProgress(
            @PathVariable Long videoId,
            @RequestBody UpdateProgressRequest request
    ) {
        videoService.updateProgress(videoId, request.getWatchedSeconds());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{videoId}/complete")
    @Operation(summary = "Mark video as completed", description = "Mark a video as fully watched")
    public ResponseEntity<Void> markAsCompleted(@PathVariable Long videoId) {
        videoService.markAsCompleted(videoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{videoId}")
    @Operation(summary = "Delete video", description = "Delete a video (admin or owner)")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long videoId) {
        videoService.deleteVideo(videoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/stream")
    @Operation(summary = "Stream video", description = "Stream video with range support and access check")
    public ResponseEntity<byte[]> streamVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String range
    ) throws Exception {

        if (!videoService.hasAccessToVideo(id)) {
            throw new RuntimeException("Access denied: You don't have access to this video");
        }

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

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

    @PostMapping(value = "/lesson/{lessonId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VideoDto>> uploadLessonVideo(
            @PathVariable Long lessonId,
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam("title") String title,
            @RequestParam("categoryId") Long categoryId
    ) throws IOException {
        return ResponseEntity.ok(videoService.uploadLessonVideo(lessonId, files, title, categoryId));
    }

    @GetMapping("/lesson/{lessonId}/category/{categoryId}")
    @Operation(summary = "Get videos by category", description = "Get lesson videos filtered by category")
    public ResponseEntity<List<VideoDto>> getVideosByCategory(
            @PathVariable Long lessonId,
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(videoService.getLessonVideosByCategory(lessonId, categoryId));
    }

    @GetMapping("/lesson/{lessonId}/homework/my")
    @Operation(summary = "Get my homework videos", description = "Get homework videos uploaded by current user")
    public ResponseEntity<List<VideoDto>> getMyHomework(@PathVariable Long lessonId) {
        return ResponseEntity.ok(videoService.getMyHomeworkVideos(lessonId));
    }
}