package com.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String keycloakId;
    private String username;
    private String email;
    private String fullName;
    private String avatar;
    private String bio;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Statistics
    private UserStatisticsResponse statistics;

    // Streak
    private UserStreakResponse streak;

    // Language Stats
    private List<LanguageStatsResponse> languageStats;

    // Recent Activity Summary
    private Integer activeDaysLast30;
    private Integer submissionsLast30;
}
