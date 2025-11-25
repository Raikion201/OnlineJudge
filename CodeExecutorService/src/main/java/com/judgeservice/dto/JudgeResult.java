package com.judgeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResult {
    private Long submissionId;
    private Long testCaseId;
    private String verdict; // ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED, etc.
    private String output;
    private String errorMessage;
    private Long executionTime; // in milliseconds
    private Long memoryUsed; // in KB
}

