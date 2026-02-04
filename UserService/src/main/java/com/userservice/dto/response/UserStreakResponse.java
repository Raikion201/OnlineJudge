package com.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStreakResponse {
    private String userId;
    private Integer currentStreak;
    private Integer maxStreak;
    private LocalDate lastActivityDate;
    private LocalDate streakStartDate;
    private Boolean isActiveToday;
}
