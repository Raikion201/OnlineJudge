package com.judgeservice.client.dto;

public class SubmissionStatusUpdateRequest {
    private String status;
    private String resultMessage;
    private Double score;
    private Long executionTime;
    private Long memoryUsage;

    public SubmissionStatusUpdateRequest() {
    }

    public SubmissionStatusUpdateRequest(String status, String resultMessage, Double score, Long executionTime, Long memoryUsage) {
        this.status = status;
        this.resultMessage = resultMessage;
        this.score = score;
        this.executionTime = executionTime;
        this.memoryUsage = memoryUsage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public Long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
}

