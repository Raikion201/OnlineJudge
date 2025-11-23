package com.judgeservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionStatusUpdateRequest {
    private String status;
    private String resultMessage;
}

