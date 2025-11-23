package com.submissionservice.submission.client;

import com.submissionservice.submission.client.dto.ProblemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "problem-service", path = "/api/v1/problems")
public interface ProblemClient {

    @GetMapping("/{id}")
    ProblemResponse getProblemById(@PathVariable Long id);
}

