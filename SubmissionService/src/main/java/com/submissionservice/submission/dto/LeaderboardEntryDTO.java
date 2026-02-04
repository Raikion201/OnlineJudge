package com.submissionservice.submission.dto;

import java.time.Instant;

public record LeaderboardEntryDTO(
        String userId,
        String username,
        Double score,
        Long executionTime,
        String language,
        Instant submittedAt
) {
}
