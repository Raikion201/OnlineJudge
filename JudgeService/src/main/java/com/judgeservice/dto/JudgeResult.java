package com.judgeservice.dto;

public class JudgeResult {
    private Long submissionId;
    private Long testCaseId;
    private String verdict; // ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED, etc.
    private String output;
    private String errorMessage;
    private Long executionTime; // in milliseconds
    private Long memoryUsed; // in KB

    public JudgeResult() {
    }

    public JudgeResult(Long submissionId, Long testCaseId, String verdict, String output, String errorMessage, Long executionTime, Long memoryUsed) {
        this.submissionId = submissionId;
        this.testCaseId = testCaseId;
        this.verdict = verdict;
        this.output = output;
        this.errorMessage = errorMessage;
        this.executionTime = executionTime;
        this.memoryUsed = memoryUsed;
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

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public Long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(Long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }
}

