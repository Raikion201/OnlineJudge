package com.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsResponse {
    private String userId;
    private Integer totalSubmissions;
    private Integer acceptedSubmissions;
    private Integer wrongAnswers;
    private Integer timeLimitExceeded;
    private Integer runtimeErrors;
    private Integer compilationErrors;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;
    private Integer totalProblemsSolved;
    private Double totalScore;
    private Double acceptanceRate;
    private Integer ranking;
    private Integer contributionPoints;
    private LocalDateTime updatedAt;
}
