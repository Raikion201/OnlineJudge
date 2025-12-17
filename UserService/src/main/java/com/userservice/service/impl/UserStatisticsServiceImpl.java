package com.userservice.service.impl;

import com.userservice.dto.response.*;
import com.userservice.entity.*;
import com.userservice.exception.ResourceNotFoundException;
import com.userservice.repository.*;
import com.userservice.service.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final UserStatisticsRepository statisticsRepository;
    private final UserActivityRepository activityRepository;
    private final UserStreakRepository streakRepository;
    private final LanguageStatsRepository languageStatsRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsResponse getStatistics(String userId) {
        UserStatistics stats = statisticsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Statistics not found for user: " + userId));
        return mapToStatisticsResponse(stats);
    }

    @Override
    @Transactional
    public UserStatisticsResponse getOrCreateStatistics(String userId) {
        UserStatistics stats = statisticsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserStatistics newStats = UserStatistics.builder()
                            .userId(userId)
                            .build();
                    return statisticsRepository.save(newStats);
                });
        return mapToStatisticsResponse(stats);
    }

    @Override
    @Transactional
    public void updateStatisticsOnSubmission(String userId, String status, String difficulty, String language) {
        log.info("Updating statistics for user {} with status {}", userId, status);

        UserStatistics stats = statisticsRepository.findByUserId(userId)
                .orElseGet(() -> UserStatistics.builder().userId(userId).build());

        stats.setTotalSubmissions(stats.getTotalSubmissions() + 1);

        switch (status.toUpperCase()) {
            case "ACCEPTED":
                stats.setAcceptedSubmissions(stats.getAcceptedSubmissions() + 1);
                if (difficulty != null) {
                    switch (difficulty.toUpperCase()) {
                        case "EASY" -> stats.setEasySolved(stats.getEasySolved() + 1);
                        case "MEDIUM" -> stats.setMediumSolved(stats.getMediumSolved() + 1);
                        case "HARD" -> stats.setHardSolved(stats.getHardSolved() + 1);
                    }
                }
                stats.setTotalProblemsSolved(stats.getEasySolved() + stats.getMediumSolved() + stats.getHardSolved());
                break;
            case "WRONG_ANSWER":
                stats.setWrongAnswers(stats.getWrongAnswers() + 1);
                break;
            case "TIME_LIMIT_EXCEEDED":
                stats.setTimeLimitExceeded(stats.getTimeLimitExceeded() + 1);
                break;
            case "RUNTIME_ERROR":
                stats.setRuntimeErrors(stats.getRuntimeErrors() + 1);
                break;
            case "COMPILATION_ERROR":
                stats.setCompilationErrors(stats.getCompilationErrors() + 1);
                break;
        }

        // Calculate acceptance rate
        if (stats.getTotalSubmissions() > 0) {
            stats.setAcceptanceRate((double) stats.getAcceptedSubmissions() / stats.getTotalSubmissions() * 100);
        }

        // Calculate score
        stats.setTotalScore(stats.getEasySolved() * 10.0 + stats.getMediumSolved() * 20.0 + stats.getHardSolved() * 40.0);

        statisticsRepository.save(stats);

        // Update language stats
        updateLanguageStats(userId, language, "ACCEPTED".equalsIgnoreCase(status));

        // Record daily activity
        recordActivity(userId, "ACCEPTED".equalsIgnoreCase(status));
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityCalendarResponse getActivityCalendar(String userId, Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days != null ? days : 365);

        List<UserActivity> activities = activityRepository
                .findByUserIdAndActivityDateBetweenOrderByActivityDateAsc(userId, startDate, endDate);

        Map<LocalDate, UserActivity> activityMap = activities.stream()
                .collect(Collectors.toMap(UserActivity::getActivityDate, a -> a));

        List<UserActivityResponse> activityResponses = new ArrayList<>();
        int totalSubmissions = 0;
        int totalAccepted = 0;

        // Generate all dates in range
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            UserActivity activity = activityMap.get(date);
            UserActivityResponse response = UserActivityResponse.builder()
                    .date(date)
                    .submissionCount(activity != null ? activity.getSubmissionCount() : 0)
                    .acceptedCount(activity != null ? activity.getAcceptedCount() : 0)
                    .problemsAttempted(activity != null ? activity.getProblemsAttempted() : 0)
                    .level(calculateActivityLevel(activity != null ? activity.getSubmissionCount() : 0))
                    .build();
            activityResponses.add(response);

            if (activity != null) {
                totalSubmissions += activity.getSubmissionCount();
                totalAccepted += activity.getAcceptedCount();
            }
        }

        Long activeDays = activityRepository.countActiveDays(userId, startDate, endDate);
        UserStreakResponse streak = getStreak(userId);

        return ActivityCalendarResponse.builder()
                .userId(userId)
                .totalActiveDays(activeDays != null ? activeDays.intValue() : 0)
                .totalSubmissions(totalSubmissions)
                .totalAccepted(totalAccepted)
                .activities(activityResponses)
                .streak(streak)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserStreakResponse getStreak(String userId) {
        UserStreak streak = streakRepository.findByUserId(userId)
                .orElse(UserStreak.builder()
                        .userId(userId)
                        .currentStreak(0)
                        .maxStreak(0)
                        .build());

        LocalDate today = LocalDate.now();
        boolean isActiveToday = streak.getLastActivityDate() != null &&
                streak.getLastActivityDate().equals(today);

        return UserStreakResponse.builder()
                .userId(userId)
                .currentStreak(streak.getCurrentStreak())
                .maxStreak(streak.getMaxStreak())
                .lastActivityDate(streak.getLastActivityDate())
                .streakStartDate(streak.getStreakStartDate())
                .isActiveToday(isActiveToday)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LanguageStatsResponse> getLanguageStats(String userId) {
        List<LanguageStats> stats = languageStatsRepository.findByUserIdOrderBySubmissionCountDesc(userId);

        int totalSubmissions = stats.stream()
                .mapToInt(LanguageStats::getSubmissionCount)
                .sum();

        return stats.stream()
                .map(ls -> LanguageStatsResponse.builder()
                        .language(ls.getLanguage())
                        .submissionCount(ls.getSubmissionCount())
                        .acceptedCount(ls.getAcceptedCount())
                        .problemsSolved(ls.getProblemsSolved())
                        .percentage(totalSubmissions > 0 ?
                                (double) ls.getSubmissionCount() / totalSubmissions * 100 : 0)
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userId) {
        User user = userRepository.findByKeycloakId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        return buildUserProfileResponse(user, userId);
    }

    @Override
    @Transactional
    public UserProfileResponse getOrCreateUserProfile(String userId, String username) {
        User user = userRepository.findByKeycloakId(userId)
                .orElseGet(() -> {
                    log.info("Creating new user with keycloakId: {} and username: {}", userId, username);
                    User newUser = User.builder()
                            .keycloakId(userId)
                            .username(username != null ? username : "user_" + userId.substring(0, 8))
                            .email(username != null ? username + "@placeholder.com" : userId + "@placeholder.com")
                            .isActive(true)
                            .build();
                    return userRepository.save(newUser);
                });

        return buildUserProfileResponse(user, userId);
    }

    private UserProfileResponse buildUserProfileResponse(User user, String userId) {
        UserStatisticsResponse stats = null;
        try {
            stats = getStatistics(userId);
        } catch (ResourceNotFoundException e) {
            stats = getOrCreateStatistics(userId);
        }

        UserStreakResponse streak = getStreak(userId);
        List<LanguageStatsResponse> languageStats = getLanguageStats(userId);

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        Long activeDays = activityRepository.countActiveDays(userId, thirtyDaysAgo, LocalDate.now());

        List<UserActivity> recentActivities = activityRepository
                .findByUserIdAndActivityDateBetweenOrderByActivityDateAsc(userId, thirtyDaysAgo, LocalDate.now());
        int submissionsLast30 = recentActivities.stream()
                .mapToInt(UserActivity::getSubmissionCount)
                .sum();

        return UserProfileResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .statistics(stats)
                .streak(streak)
                .languageStats(languageStats)
                .activeDaysLast30(activeDays != null ? activeDays.intValue() : 0)
                .submissionsLast30(submissionsLast30)
                .build();
    }

    @Override
    @Transactional
    public void recordActivity(String userId, boolean accepted) {
        LocalDate today = LocalDate.now();

        UserActivity activity = activityRepository.findByUserIdAndActivityDate(userId, today)
                .orElseGet(() -> UserActivity.builder()
                        .userId(userId)
                        .activityDate(today)
                        .build());

        activity.setSubmissionCount(activity.getSubmissionCount() + 1);
        if (accepted) {
            activity.setAcceptedCount(activity.getAcceptedCount() + 1);
        }
        activity.setProblemsAttempted(activity.getProblemsAttempted() + 1);

        activityRepository.save(activity);

        // Update streak
        updateStreak(userId, today);
    }

    private void updateStreak(String userId, LocalDate today) {
        UserStreak streak = streakRepository.findByUserId(userId)
                .orElseGet(() -> UserStreak.builder()
                        .userId(userId)
                        .currentStreak(0)
                        .maxStreak(0)
                        .build());

        LocalDate lastActivity = streak.getLastActivityDate();

        if (lastActivity == null) {
            // First activity
            streak.setCurrentStreak(1);
            streak.setStreakStartDate(today);
        } else if (lastActivity.equals(today)) {
            // Already recorded today, no change
        } else if (lastActivity.equals(today.minusDays(1))) {
            // Consecutive day
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            // Streak broken, start new
            streak.setCurrentStreak(1);
            streak.setStreakStartDate(today);
        }

        streak.setLastActivityDate(today);

        if (streak.getCurrentStreak() > streak.getMaxStreak()) {
            streak.setMaxStreak(streak.getCurrentStreak());
        }

        streakRepository.save(streak);
    }

    private void updateLanguageStats(String userId, String language, boolean accepted) {
        if (language == null || language.isEmpty()) return;

        LanguageStats stats = languageStatsRepository.findByUserIdAndLanguage(userId, language)
                .orElseGet(() -> LanguageStats.builder()
                        .userId(userId)
                        .language(language)
                        .build());

        stats.setSubmissionCount(stats.getSubmissionCount() + 1);
        if (accepted) {
            stats.setAcceptedCount(stats.getAcceptedCount() + 1);
            stats.setProblemsSolved(stats.getProblemsSolved() + 1);
        }

        languageStatsRepository.save(stats);
    }

    private UserStatisticsResponse mapToStatisticsResponse(UserStatistics stats) {
        return UserStatisticsResponse.builder()
                .userId(stats.getUserId())
                .totalSubmissions(stats.getTotalSubmissions())
                .acceptedSubmissions(stats.getAcceptedSubmissions())
                .wrongAnswers(stats.getWrongAnswers())
                .timeLimitExceeded(stats.getTimeLimitExceeded())
                .runtimeErrors(stats.getRuntimeErrors())
                .compilationErrors(stats.getCompilationErrors())
                .easySolved(stats.getEasySolved())
                .mediumSolved(stats.getMediumSolved())
                .hardSolved(stats.getHardSolved())
                .totalProblemsSolved(stats.getTotalProblemsSolved())
                .totalScore(stats.getTotalScore())
                .acceptanceRate(stats.getAcceptanceRate())
                .ranking(stats.getRanking())
                .contributionPoints(stats.getContributionPoints())
                .updatedAt(stats.getUpdatedAt())
                .build();
    }

    private int calculateActivityLevel(int submissionCount) {
        if (submissionCount == 0) return 0;
        if (submissionCount <= 2) return 1;
        if (submissionCount <= 5) return 2;
        if (submissionCount <= 10) return 3;
        return 4;
    }
}
