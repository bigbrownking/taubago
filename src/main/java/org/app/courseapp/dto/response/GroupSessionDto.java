package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupSessionDto {
    private Long id;
    private Long specialistId;
    private String specialistName;
    private String specialistProfession;
    private String profilePictureUrl;
    private String title;
    private String description;
    private String telegramLink;
    private LocalDateTime scheduledAt;
    private Integer maxParticipants;
}