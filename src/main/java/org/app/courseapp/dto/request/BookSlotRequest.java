package org.app.courseapp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookSlotRequest {
    @NotNull
    private Long slotId;
}
