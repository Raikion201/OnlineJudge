package com.onlinejudgeservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseRequest {
    @NotBlank(message = "Input cannot be empty")
    private String input;

    @NotBlank(message = "Expected output cannot be empty")
    private String expectedOutput;

    private boolean isSample;

    @Min(value = 1, message = "Points must be at least 1")
    private int points;

    private Integer timeLimit;

    private Integer memoryLimit;

    private int ordering;
}