package com.judgeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeExecuteRequest {
    private Long submissionId;
    private Long testCaseId;
    private String code;
    private String language;
    private String input;
    private String expectedOutput;
    private Integer timeLimit;
    private Integer memoryLimit;

    // LeetCode-style function metadata
    private String functionName;
    private String functionSignature;
    private boolean leetcodeStyle;
}

