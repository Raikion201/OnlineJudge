package com.userservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkRequest {
    @NotNull(message = "Problem ID is required")
    private Long problemId;

    private String problemTitle;

    private String difficulty;

    private String note;
}
