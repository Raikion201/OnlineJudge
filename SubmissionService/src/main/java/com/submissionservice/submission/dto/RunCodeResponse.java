package com.submissionservice.submission.dto;

import java.util.List;

public record RunCodeResponse(
        String status,
        List<TestCaseResult> testCaseResults,
        Long totalExecutionTime,
        Long maxMemoryUsed,
        String errorMessage
) {
    public record TestCaseResult(
            Integer testCaseNumber,
            String input,
            String expectedOutput,
            String actualOutput,
            Boolean passed,
            Long executionTime,
            Long memoryUsed,
            String status,
            String errorMessage
    ) {}
}
