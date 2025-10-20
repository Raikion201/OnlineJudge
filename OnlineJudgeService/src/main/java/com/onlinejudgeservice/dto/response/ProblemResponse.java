package com.onlinejudgeservice.dto.response;

import com.onlinejudgeservice.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemResponse {
    private Long id;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private Difficulty difficulty;
    private int timeLimit;
    private int memoryLimit;
    private boolean isPublic;
    private double acceptanceRate;
    private int totalSubmissions;
    private int totalAccepted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<TestCaseResponse> testCases = new HashSet<>();
    private Set<ProblemExampleResponse> examples = new HashSet<>();
    private Set<TagResponse> tags = new HashSet<>();
}