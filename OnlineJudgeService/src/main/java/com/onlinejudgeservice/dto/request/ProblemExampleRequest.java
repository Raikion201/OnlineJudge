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
public class ProblemExampleRequest {
    @NotBlank(message = "Input cannot be empty")
    private String input;

    @NotBlank(message = "Output cannot be empty")
    private String output;

    private String explanation;

    @Min(value = 0, message = "Ordering must be non-negative")
    private int ordering = 0;
}