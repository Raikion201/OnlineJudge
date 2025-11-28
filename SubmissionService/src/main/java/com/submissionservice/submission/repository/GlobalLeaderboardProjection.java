package com.submissionservice.submission.repository;

import java.time.Instant;

public interface GlobalLeaderboardProjection {
    String getUserId();
    Double getTotalScore();
    Long getTotalAccepted();
    Long getTotalSubmissions();
    Instant getLastSubmission();
}
