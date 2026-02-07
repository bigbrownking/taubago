package org.app.courseapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.app.courseapp.model.CourseMonth;
import org.app.courseapp.model.Lesson;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private CourseMonth month;
    private List<Lesson> lessons;
    private String monthDisplayName;
    private Integer durationDays;
    private String createdByName;
    private LocalDateTime createdAt;
    private Boolean isEnrolled;
    private Integer enrolledCount;
}