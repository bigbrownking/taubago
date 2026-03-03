package org.app.courseapp.model.users;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.app.courseapp.model.VideoCategory;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "parents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Parent extends User {

    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Child> children = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "parent_video_access",
            joinColumns = @JoinColumn(name = "parent_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<VideoCategory> allowedVideoCategories = new HashSet<>();

    public void addChild(Child child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Child child) {
        children.remove(child);
        child.setParent(null);
    }

    public void grantVideoAccess(VideoCategory category) {
        allowedVideoCategories.add(category);
    }

    public void revokeVideoAccess(VideoCategory category) {
        allowedVideoCategories.remove(category);
    }

    public boolean hasAccessToCategory(VideoCategory category) {
        return allowedVideoCategories.contains(category);
    }

    public boolean hasAccessToCategoryById(Long categoryId) {
        return allowedVideoCategories.stream()
                .anyMatch(cat -> cat.getId().equals(categoryId));
    }
}