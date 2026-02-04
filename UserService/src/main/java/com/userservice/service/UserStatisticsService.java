package com.userservice.service;

import com.userservice.dto.response.*;

import java.util.List;

public interface UserStatisticsService {

    UserStatisticsResponse getStatistics(String userId);

    UserStatisticsResponse getOrCreateStatistics(String userId);

    void updateStatisticsOnSubmission(String userId, String status, String difficulty, String language);

    ActivityCalendarResponse getActivityCalendar(String userId, Integer days);

    UserStreakResponse getStreak(String userId);

    List<LanguageStatsResponse> getLanguageStats(String userId);

    UserProfileResponse getUserProfile(String userId);

    UserProfileResponse getOrCreateUserProfile(String userId, String username);

    void recordActivity(String userId, boolean accepted);
}
