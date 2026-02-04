package com.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageStatsResponse {
    private String language;
    private Integer submissionCount;
    private Integer acceptedCount;
    private Integer problemsSolved;
    private Double percentage; // Percentage of total submissions
}
