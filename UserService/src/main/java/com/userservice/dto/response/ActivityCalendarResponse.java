package com.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityCalendarResponse {
    private String userId;
    private Integer totalActiveDays;
    private Integer totalSubmissions;
    private Integer totalAccepted;
    private List<UserActivityResponse> activities;
    private UserStreakResponse streak;
}
