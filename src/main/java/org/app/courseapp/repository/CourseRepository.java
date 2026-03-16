package org.app.courseapp.repository;

import org.app.courseapp.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseOrder(int order);
    List<Course> findAllByOrderByCourseOrderAsc();
    boolean existsByTitle(String title);

    @Query("SELECT COALESCE(MAX(c.courseOrder), 0) + 1 FROM Course c")
    int findNextCourseOrder();
}