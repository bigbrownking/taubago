package org.app.courseapp.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GrantVideoCategoryAccessRequest {

    @NotNull(message = "Parent ID is required")
    private Long parentId;

    @NotEmpty(message = "At least one category ID is required")
    private List<Long> categoryIds;
}