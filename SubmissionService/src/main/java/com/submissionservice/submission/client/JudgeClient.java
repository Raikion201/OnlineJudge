package com.submissionservice.submission.client;

import com.submissionservice.submission.client.dto.SubmissionDispatchRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "judge-service", path = "/internal/v1/judge")
public interface JudgeClient {

    @PostMapping("/jobs")
    void dispatchSubmission(@RequestBody SubmissionDispatchRequest request);
}

