package com.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityResponse {
    private LocalDate date;
    private Integer submissionCount;
    private Integer acceptedCount;
    private Integer problemsAttempted;
    private Integer level; // 0-4 for GitHub-style heatmap coloring
}
