package com.onlinejudgeservice.exception;

import com.onlinejudgeservice.enums.ErrorType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private ErrorType error;
    private String message;
    private String path;

    @Builder.Default
    private List<ValidationError> validationErrors = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}