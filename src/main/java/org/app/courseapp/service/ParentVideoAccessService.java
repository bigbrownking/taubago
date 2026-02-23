package org.app.courseapp.service;

import org.app.courseapp.dto.response.ParentVideoAccessDto;
import org.app.courseapp.dto.response.VideoCategoryDto;

import java.util.List;

public interface ParentVideoAccessService {
    void grantAccess(Long parentId, List<Long> categoryIds);
    void revokeAccess(Long parentId, List<Long> categoryIds);
    void setAccess(Long parentId, List<Long> categoryIds);
    ParentVideoAccessDto getParentAccess(Long parentId);
    List<VideoCategoryDto> getMyAllowedCategories();
    boolean hasAccessToVideo(Long videoId);
}