package com.submissionservice.submission.model;

public enum SubmissionStatus {
    PENDING,
    DISPATCHED,
    RUNNING,
    ACCEPTED,
    WRONG_ANSWER,
    TIME_LIMIT_EXCEEDED,
    MEMORY_LIMIT_EXCEEDED,
    COMPILATION_ERROR,
    RUNTIME_ERROR,
    SYSTEM_ERROR
}

