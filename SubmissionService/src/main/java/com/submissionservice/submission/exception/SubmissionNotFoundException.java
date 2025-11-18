package com.submissionservice.submission.exception;

public class SubmissionNotFoundException extends RuntimeException {
    public SubmissionNotFoundException(Long id) {
        super("Submission %d not found".formatted(id));
    }
}

