package com.submissionservice.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RunCodeRequest(
        @NotNull(message = "Problem ID is required")
        Long problemId,

        @NotBlank(message = "Language is required")
        String language,

        @NotBlank(message = "Code is required")
        String code,

        String customInput
) {}
