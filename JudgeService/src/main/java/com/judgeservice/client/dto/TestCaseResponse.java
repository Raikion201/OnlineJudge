package com.judgeservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
}

