package com.onlinejudgeservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResponse {
    private Long id;
    private String input;
    private String expectedOutput;
    private boolean isSample;
    private int points;
    private Integer timeLimit;
    private Integer memoryLimit;
    private int ordering;
    private LocalDateTime createdAt;
}