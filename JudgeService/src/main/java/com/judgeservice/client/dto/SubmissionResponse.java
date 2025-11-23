package com.judgeservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long id;
    private Long problemId;
    private String userId;
    private String language;
    private String code;
    private String status;
    private String resultMessage;
    private Instant createdAt;
    private Instant updatedAt;
}

