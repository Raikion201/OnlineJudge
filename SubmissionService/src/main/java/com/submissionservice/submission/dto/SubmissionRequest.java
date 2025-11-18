package com.submissionservice.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmissionRequest(
        @NotNull(message = "problemId is required")
        Long problemId,

        @NotBlank(message = "language is required")
        @Size(max = 32, message = "language must be <= 32 characters")
        String language,

        @NotBlank(message = "code is required")
        String code
) {
}

