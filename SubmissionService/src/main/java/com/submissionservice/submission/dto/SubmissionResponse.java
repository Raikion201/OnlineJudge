package com.submissionservice.submission.dto;

import com.submissionservice.submission.model.SubmissionStatus;

import java.time.Instant;

public record SubmissionResponse(
        Long id,
        Long problemId,
        String userId,
        String language,
        String code,
        SubmissionStatus status,
        String resultMessage,
        Instant createdAt,
        Instant updatedAt
) {
}

