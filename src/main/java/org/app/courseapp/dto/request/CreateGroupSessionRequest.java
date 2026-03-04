package org.app.courseapp.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateGroupSessionRequest {
    private String title;
    private String description;
    private String telegramLink;
    private LocalDateTime scheduledAt;
    private Integer maxParticipants;
}
