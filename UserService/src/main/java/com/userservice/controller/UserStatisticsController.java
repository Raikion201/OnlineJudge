package com.userservice.controller;

import com.userservice.dto.response.*;
import com.userservice.service.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserStatisticsController {

    private static final String USER_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private final UserStatisticsService statisticsService;

    @GetMapping("/me/statistics")
    public ResponseEntity<UserStatisticsResponse> getMyStatistics(
            @RequestHeader(USER_HEADER) String userId) {
        return ResponseEntity.ok(statisticsService.getOrCreateStatistics(userId));
    }

    @GetMapping("/{userId}/statistics")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(@PathVariable String userId) {
        return ResponseEntity.ok(statisticsService.getOrCreateStatistics(userId));
    }

    @GetMapping("/me/activity-calendar")
    public ResponseEntity<ActivityCalendarResponse> getMyActivityCalendar(
            @RequestHeader(USER_HEADER) String userId,
            @RequestParam(defaultValue = "365") Integer days) {
        return ResponseEntity.ok(statisticsService.getActivityCalendar(userId, days));
    }

    @GetMapping("/{userId}/activity-calendar")
    public ResponseEntity<ActivityCalendarResponse> getUserActivityCalendar(
            @PathVariable String userId,
            @RequestParam(defaultValue = "365") Integer days) {
        return ResponseEntity.ok(statisticsService.getActivityCalendar(userId, days));
    }

    @GetMapping("/me/streak")
    public ResponseEntity<UserStreakResponse> getMyStreak(
            @RequestHeader(USER_HEADER) String userId) {
        return ResponseEntity.ok(statisticsService.getStreak(userId));
    }

    @GetMapping("/{userId}/streak")
    public ResponseEntity<UserStreakResponse> getUserStreak(@PathVariable String userId) {
        return ResponseEntity.ok(statisticsService.getStreak(userId));
    }

    @GetMapping("/me/language-stats")
    public ResponseEntity<List<LanguageStatsResponse>> getMyLanguageStats(
            @RequestHeader(USER_HEADER) String userId) {
        return ResponseEntity.ok(statisticsService.getLanguageStats(userId));
    }

    @GetMapping("/{userId}/language-stats")
    public ResponseEntity<List<LanguageStatsResponse>> getUserLanguageStats(@PathVariable String userId) {
        return ResponseEntity.ok(statisticsService.getLanguageStats(userId));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @RequestHeader(USER_HEADER) String userId,
            @RequestHeader(value = USER_NAME_HEADER, required = false) String username) {
        return ResponseEntity.ok(statisticsService.getOrCreateUserProfile(userId, username));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String userId) {
        return ResponseEntity.ok(statisticsService.getUserProfile(userId));
    }

    // Internal endpoint to update statistics from submission service
    @PostMapping("/internal/statistics/update")
    public ResponseEntity<Void> updateStatistics(
            @RequestParam String userId,
            @RequestParam String status,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String language) {
        statisticsService.updateStatisticsOnSubmission(userId, status, difficulty, language);
        return ResponseEntity.ok().build();
    }
}
