package com.submissionservice.submission.dto;

import com.submissionservice.submission.model.SubmissionStatus;
import jakarta.validation.constraints.NotNull;

public record SubmissionStatusUpdateRequest(
        @NotNull
        SubmissionStatus status,
        String resultMessage
) {
}

