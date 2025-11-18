package com.submissionservice.submission.controller;

import com.submissionservice.submission.dto.SubmissionRequest;
import com.submissionservice.submission.dto.SubmissionResponse;
import com.submissionservice.submission.service.SubmissionApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private static final String USER_HEADER = "X-User-Id";
    private final SubmissionApplicationService submissionApplicationService;

    public SubmissionController(SubmissionApplicationService submissionApplicationService) {
        this.submissionApplicationService = submissionApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SubmissionResponse createSubmission(@Valid @RequestBody SubmissionRequest request,
                                               @RequestHeader(USER_HEADER) String userId) {
        return submissionApplicationService.createSubmission(request, userId);
    }

    @GetMapping("/{id}")
    public SubmissionResponse getSubmission(@PathVariable Long id,
                                            @RequestHeader(USER_HEADER) String userId) {
        return submissionApplicationService.getSubmissionForUser(id, userId);
    }

    @GetMapping("/user/me")
    public List<SubmissionResponse> getMySubmissions(@RequestHeader(USER_HEADER) String userId) {
        return submissionApplicationService.getUserSubmissions(userId);
    }
}

