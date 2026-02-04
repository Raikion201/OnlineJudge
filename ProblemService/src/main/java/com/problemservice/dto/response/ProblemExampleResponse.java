package com.problemservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemExampleResponse {
    private Long id;
    private String input;
    private String output;
    private String explanation;
    private int ordering;
    private LocalDateTime createdAt;
}