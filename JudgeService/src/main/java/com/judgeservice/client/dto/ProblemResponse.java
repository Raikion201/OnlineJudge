package com.judgeservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemResponse {
    private Long id;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private String difficulty;
    private int timeLimit;
    private int memoryLimit;
    private boolean isPublic;
    private double acceptanceRate;
    private int totalSubmissions;
    private int totalAccepted;
    private Set<TestCaseResponse> testCases;
}

