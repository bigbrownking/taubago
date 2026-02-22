package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.model.VideoCategory;
import org.app.courseapp.repository.VideoCategoryRepository;
import org.app.courseapp.service.VideoCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoCategoryServiceImpl implements VideoCategoryService {

    private final VideoCategoryRepository categoryRepository;

    @Override
    public VideoCategory getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public VideoCategory getByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found: " + name));
    }

    @Override
    public List<VideoCategory> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public void create(List<String> names) {
        categoryRepository.saveAll(
                names.stream()
                        .map(name -> {
                            VideoCategory category = new VideoCategory();
                            category.setName(name);
                            return category;
                        })
                        .toList()
        );
    }
}
