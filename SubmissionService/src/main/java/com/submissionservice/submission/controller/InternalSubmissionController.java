package com.submissionservice.submission.controller;

import com.submissionservice.submission.dto.SubmissionResponse;
import com.submissionservice.submission.dto.SubmissionStatusUpdateRequest;
import com.submissionservice.submission.service.SubmissionApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/submissions")
public class InternalSubmissionController {

    private final SubmissionApplicationService submissionApplicationService;

    public InternalSubmissionController(SubmissionApplicationService submissionApplicationService) {
        this.submissionApplicationService = submissionApplicationService;
    }

    @GetMapping("/{id}")
    public SubmissionResponse getSubmission(@PathVariable Long id) {
        return submissionApplicationService.getSubmissionInternal(id);
    }

    @PutMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(@PathVariable Long id,
                             @Valid @RequestBody SubmissionStatusUpdateRequest request) {
        submissionApplicationService.updateSubmissionStatus(id, request);
    }
}

