package com.problemservice.controller;

import com.problemservice.dto.request.ProblemExampleRequest;
import com.problemservice.dto.request.TestCaseRequest;
import com.problemservice.dto.response.ProblemExampleResponse;
import com.problemservice.dto.response.TestCaseResponse;
import com.problemservice.service.TestCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/problems/{problemId}")
@RequiredArgsConstructor
public class TestCaseController {
    private final TestCaseService testCaseService;

    // Example endpoints
    @PostMapping("/examples")
    public ResponseEntity<ProblemExampleResponse> addExample(
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemExampleRequest request) {
        log.info("POST /examples for problemId: {}", problemId);
        ProblemExampleResponse response = testCaseService.addExample(problemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/examples")
    public ResponseEntity<List<ProblemExampleResponse>> getExamples(@PathVariable Long problemId) {
        log.info("GET /examples for problemId: {}", problemId);
        List<ProblemExampleResponse> responses = testCaseService.getExamples(problemId);
        log.info("Returning {} examples", responses.size());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/examples/{exampleId}")
    public ResponseEntity<ProblemExampleResponse> updateExample(
            @PathVariable Long problemId,
            @PathVariable Long exampleId,
            @Valid @RequestBody ProblemExampleRequest request) {
        log.info("PUT /examples/{} for problemId: {}", exampleId, problemId);
        ProblemExampleResponse response = testCaseService.updateExample(problemId, exampleId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/examples/{exampleId}")
    public ResponseEntity<Void> deleteExample(
            @PathVariable Long problemId,
            @PathVariable Long exampleId) {
        log.info("DELETE /examples/{} for problemId: {}", exampleId, problemId);
        testCaseService.deleteExample(problemId, exampleId);
        return ResponseEntity.noContent().build();
    }

    // TestCase endpoints
    @PostMapping("/testcases")
    public ResponseEntity<TestCaseResponse> addTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody TestCaseRequest request) {
        log.info("POST /testcases for problemId: {}", problemId);
        TestCaseResponse response = testCaseService.addTestCase(problemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/testcases")
    public ResponseEntity<List<TestCaseResponse>> getTestCases(@PathVariable Long problemId) {
        log.info("GET /testcases for problemId: {}", problemId);
        List<TestCaseResponse> responses = testCaseService.getTestCases(problemId);
        log.info("Returning {} test cases", responses.size());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/testcases/{testCaseId}")
    public ResponseEntity<TestCaseResponse> updateTestCase(
            @PathVariable Long problemId,
            @PathVariable Long testCaseId,
            @Valid @RequestBody TestCaseRequest request) {
        log.info("PUT /testcases/{} for problemId: {}", testCaseId, problemId);
        TestCaseResponse response = testCaseService.updateTestCase(problemId, testCaseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/testcases/{testCaseId}")
    public ResponseEntity<Void> deleteTestCase(
            @PathVariable Long problemId,
            @PathVariable Long testCaseId) {
        log.info("DELETE /testcases/{} for problemId: {}", testCaseId, problemId);
        testCaseService.deleteTestCase(problemId, testCaseId);
        return ResponseEntity.noContent().build();
    }
}
