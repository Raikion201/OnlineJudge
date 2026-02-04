package com.judgeservice.client.dto;

import java.time.Instant;

public class SubmissionResponse {
    private Long id;
    private Long problemId;
    private String userId;
    private String language;
    private String code;
    private String status;
    private String resultMessage;
    private Instant createdAt;
    private Instant updatedAt;

    public SubmissionResponse() {
    }

    public SubmissionResponse(Long id, Long problemId, String userId, String language, String code, String status, String resultMessage, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.problemId = problemId;
        this.userId = userId;
        this.language = language;
        this.code = code;
        this.status = status;
        this.resultMessage = resultMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

