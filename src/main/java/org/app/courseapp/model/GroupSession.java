package org.app.courseapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.app.courseapp.model.users.Specialist;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_sessions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GroupSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private Specialist specialist;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "telegram_link", nullable = false)
    private String telegramLink;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}