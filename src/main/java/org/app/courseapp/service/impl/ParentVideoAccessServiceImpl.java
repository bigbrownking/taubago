package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.response.ParentVideoAccessDto;
import org.app.courseapp.dto.response.VideoCategoryDto;
import org.app.courseapp.model.Video;
import org.app.courseapp.model.VideoCategory;
import org.app.courseapp.model.users.Parent;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.ParentRepository;
import org.app.courseapp.repository.VideoCategoryRepository;
import org.app.courseapp.repository.VideoRepository;
import org.app.courseapp.service.ParentVideoAccessService;
import org.app.courseapp.service.UserService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentVideoAccessServiceImpl implements ParentVideoAccessService {

    private final ParentRepository parentRepository;
    private final VideoCategoryRepository videoCategoryRepository;
    private final VideoRepository videoRepository;
    private final UserService userService;

    private final Mapper mapper;

    @Override
    @Transactional
    public void grantAccess(Long parentId, List<Long> categoryIds) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        List<VideoCategory> categories = videoCategoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new RuntimeException("Some categories not found");
        }

        categories.forEach(parent::grantVideoAccess);
        parentRepository.save(parent);

        log.info("Admin granted video access to parent {} for categories: {}",
                parent.getEmail(), categoryIds);
    }

    @Override
    @Transactional
    public void revokeAccess(Long parentId, List<Long> categoryIds) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        List<VideoCategory> categories = videoCategoryRepository.findAllById(categoryIds);

        categories.forEach(parent::revokeVideoAccess);
        parentRepository.save(parent);

        log.info("Admin revoked video access from parent {} for categories: {}",
                parent.getEmail(), categoryIds);
    }

    @Override
    @Transactional
    public void setAccess(Long parentId, List<Long> categoryIds) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        List<VideoCategory> categories = videoCategoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new RuntimeException("Some categories not found");
        }

        // Очищаем старые доступы и устанавливаем новые
        parent.getAllowedVideoCategories().clear();
        categories.forEach(parent::grantVideoAccess);
        parentRepository.save(parent);

        log.info("Admin set video access for parent {} to categories: {}",
                parent.getEmail(), categoryIds);
    }

    @Override
    @Transactional(readOnly = true)
    public ParentVideoAccessDto getParentAccess(Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        List<VideoCategory> allCategories = videoCategoryRepository.findAll();
        Set<Long> allowedCategoryIds = parent.getAllowedVideoCategories().stream()
                .map(VideoCategory::getId)
                .collect(Collectors.toSet());

        List<VideoCategoryDto> allCategoriesDto = allCategories.stream()
                .map(cat -> VideoCategoryDto.fromEntity(cat, allowedCategoryIds.contains(cat.getId())))
                .collect(Collectors.toList());

        List<VideoCategoryDto> allowedCategoriesDto = parent.getAllowedVideoCategories().stream()
                .map(VideoCategoryDto::fromEntity)
                .collect(Collectors.toList());

        return ParentVideoAccessDto.builder()
                .parentId(parent.getId())
                .parentName(mapper.resolveUserName(parent))
                .parentEmail(parent.getEmail())
                .allowedCategories(allowedCategoriesDto)
                .allCategories(allCategoriesDto)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoCategoryDto> getMyAllowedCategories() {
        User currentUser = userService.getCurrentUser();

        if (!(currentUser instanceof Parent)) {
            throw new RuntimeException("Only parents have video category access");
        }

        Parent parent = parentRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        return parent.getAllowedVideoCategories().stream()
                .map(VideoCategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToVideo(Long videoId) {
        User currentUser = userService.getCurrentUser();

        if (!(currentUser instanceof Parent)) {
            return false;
        }

        Parent parent = parentRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        if (video.getCategory() == null) {
            return true;
        }

        return parent.hasAccessToCategory(video.getCategory());
    }
}