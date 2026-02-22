package org.app.courseapp.service;

import org.app.courseapp.model.VideoCategory;

import java.util.List;

public interface VideoCategoryService {
    VideoCategory getById(Long id);
    VideoCategory getByName(String name);
    List<VideoCategory> getAll();
    void create(List<String> name);
}
