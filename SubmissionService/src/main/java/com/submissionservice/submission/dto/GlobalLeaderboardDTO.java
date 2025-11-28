package com.submissionservice.submission.dto;

import java.time.Instant;

public class GlobalLeaderboardDTO {
    private int rank;
    private String userId;
    private String username; // To be filled by service
    private Double totalScore;
    private Long totalAccepted;
    private Long totalSubmissions;
    private Instant lastSubmission;

    public GlobalLeaderboardDTO(String userId, Double totalScore, Long totalAccepted, Long totalSubmissions, Instant lastSubmission) {
        this.userId = userId;
        this.totalScore = totalScore;
        this.totalAccepted = totalAccepted;
        this.totalSubmissions = totalSubmissions;
        this.lastSubmission = lastSubmission;
    }

    // Getters and Setters
    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Long getTotalAccepted() {
        return totalAccepted;
    }

    public void setTotalAccepted(Long totalAccepted) {
        this.totalAccepted = totalAccepted;
    }

    public Long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(Long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public Instant getLastSubmission() {
        return lastSubmission;
    }

    public void setLastSubmission(Instant lastSubmission) {
        this.lastSubmission = lastSubmission;
    }
    
    public Double getAcceptanceRate() {
        if (totalSubmissions == 0) return 0.0;
        return (double) totalAccepted / totalSubmissions * 100.0;
    }
}
