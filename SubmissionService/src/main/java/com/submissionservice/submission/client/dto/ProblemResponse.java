package com.submissionservice.submission.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemResponse {
    private Long id;
    private String title;
    private String difficulty;
    private int timeLimit;
    private int memoryLimit;
    private Set<ProblemExampleDTO> examples = new HashSet<>();

    // LeetCode-style function metadata
    private String functionName;
    private String functionSignature;
    private String codeTemplate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemExampleDTO {
        private Long id;
        private String input;
        private String output;
        private String explanation;
        private int ordering;
    }
}

