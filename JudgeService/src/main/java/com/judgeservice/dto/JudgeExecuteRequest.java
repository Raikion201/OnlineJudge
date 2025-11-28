package com.judgeservice.dto;

public class JudgeExecuteRequest {
    private Long submissionId;
    private Long testCaseId;
    private String code;
    private String language;
    private String input;
    private String expectedOutput;
    private Integer timeLimit;
    private Integer memoryLimit;

    public JudgeExecuteRequest() {
    }

    public JudgeExecuteRequest(Long submissionId, Long testCaseId, String code, String language, String input, String expectedOutput, Integer timeLimit, Integer memoryLimit) {
        this.submissionId = submissionId;
        this.testCaseId = testCaseId;
        this.code = code;
        this.language = language;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.timeLimit = timeLimit;
        this.memoryLimit = memoryLimit;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Long getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(Long testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(Integer memoryLimit) {
        this.memoryLimit = memoryLimit;
    }
}

