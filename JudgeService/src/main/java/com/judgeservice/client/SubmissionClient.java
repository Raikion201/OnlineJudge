package com.judgeservice.client;

import com.judgeservice.client.dto.SubmissionResponse;
import com.judgeservice.client.dto.SubmissionStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "submission-service", path = "/internal/v1/submissions")
public interface SubmissionClient {

    @GetMapping("/{id}")
    SubmissionResponse getSubmission(@PathVariable Long id);

    @PutMapping("/{id}/status")
    void updateSubmissionStatus(@PathVariable Long id, @RequestBody SubmissionStatusUpdateRequest request);
}

