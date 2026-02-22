package org.app.courseapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "video_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}