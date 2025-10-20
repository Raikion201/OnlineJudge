package com.onlinejudgeservice.dto.request;

import com.onlinejudgeservice.enums.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemRequest {
    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    private String inputFormat;
    private String outputFormat;
    private String constraints;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @Min(value = 100, message = "Time limit must be at least 100ms")
    private int timeLimit = 1000;

    @Min(value = 16, message = "Memory limit must be at least 16MB")
    private int memoryLimit = 256;

    private boolean isPublic = false;

    @Valid
    private Set<TestCaseRequest> testCases = new HashSet<>();

    @Valid
    private Set<ProblemExampleRequest> examples = new HashSet<>();

    private Set<Long> tagIds = new HashSet<>();
}