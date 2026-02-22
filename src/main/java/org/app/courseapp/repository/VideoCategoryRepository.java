package org.app.courseapp.repository;

import org.app.courseapp.model.VideoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoCategoryRepository extends JpaRepository<VideoCategory, Long> {
    Optional<VideoCategory> findByName(String name);
}
