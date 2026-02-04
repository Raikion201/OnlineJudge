package com.submissionservice.submission.service;

import com.submissionservice.submission.client.ProblemClient;
import com.submissionservice.submission.client.dto.ProblemResponse;
import com.submissionservice.submission.dto.RunCodeRequest;
import com.submissionservice.submission.dto.RunCodeResponse;
import com.submissionservice.submission.exception.BadRequestException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class RunCodeService {

    private static final Logger log = LoggerFactory.getLogger(RunCodeService.class);
    private static final String RUN_CODE_TOPIC = "run.code";
    private static final String RUN_CODE_RESULTS_TOPIC = "run.code.results";

    private final ProblemClient problemClient;
    private final KafkaTemplate<String, Object> kafkaObjectTemplate;

    // Store for pending run requests - maps requestId to CompletableFuture
    private final Map<String, CompletableFuture<RunCodeResponse>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, List<RunCodeResponse.TestCaseResult>> partialResults = new ConcurrentHashMap<>();
    private final Map<String, Integer> expectedCounts = new ConcurrentHashMap<>();

    public RunCodeService(ProblemClient problemClient, KafkaTemplate<String, Object> kafkaObjectTemplate) {
        this.problemClient = problemClient;
        this.kafkaObjectTemplate = kafkaObjectTemplate;
    }

    public RunCodeResponse runCode(RunCodeRequest request, String userId) {
        log.info("Running code for user {} on problem {}", userId, request.problemId());

        // Validate and get problem
        ProblemResponse problem;
        try {
            problem = problemClient.getProblemById(request.problemId());
        } catch (FeignException.NotFound ex) {
            throw new BadRequestException("Problem not found with id: " + request.problemId());
        }

        if (problem.getExamples() == null || problem.getExamples().isEmpty()) {
            throw new BadRequestException("No sample test cases available for this problem");
        }

        String requestId = UUID.randomUUID().toString();
        CompletableFuture<RunCodeResponse> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        partialResults.put(requestId, Collections.synchronizedList(new ArrayList<>()));

        List<ProblemResponse.ProblemExampleDTO> examples = new ArrayList<>(problem.getExamples());
        examples.sort(Comparator.comparingInt(ProblemResponse.ProblemExampleDTO::getOrdering));

        // Check if this is a LeetCode-style problem
        boolean leetcodeStyle = problem.getFunctionName() != null && !problem.getFunctionName().isEmpty()
                && problem.getFunctionSignature() != null && !problem.getFunctionSignature().isEmpty();

        // If custom input provided, use only that
        if (request.customInput() != null && !request.customInput().isBlank()) {
            expectedCounts.put(requestId, 1);

            Map<String, Object> runRequest = new HashMap<>();
            runRequest.put("requestId", requestId);
            runRequest.put("testCaseNumber", 1);
            runRequest.put("totalTestCases", 1);
            runRequest.put("code", request.code());
            runRequest.put("language", request.language());
            runRequest.put("input", request.customInput());
            runRequest.put("expectedOutput", ""); // No expected output for custom input
            runRequest.put("timeLimit", problem.getTimeLimit());
            runRequest.put("memoryLimit", problem.getMemoryLimit());

            // LeetCode-style function metadata
            runRequest.put("leetcodeStyle", leetcodeStyle);
            runRequest.put("functionName", problem.getFunctionName());
            runRequest.put("functionSignature", problem.getFunctionSignature());

            publishRunCodeRequest(runRequest);
        } else {
            expectedCounts.put(requestId, examples.size());

            int testCaseNumber = 1;
            for (ProblemResponse.ProblemExampleDTO example : examples) {
                Map<String, Object> runRequest = new HashMap<>();
                runRequest.put("requestId", requestId);
                runRequest.put("testCaseNumber", testCaseNumber);
                runRequest.put("totalTestCases", examples.size());
                runRequest.put("code", request.code());
                runRequest.put("language", request.language());
                runRequest.put("input", example.getInput());
                runRequest.put("expectedOutput", example.getOutput());
                runRequest.put("timeLimit", problem.getTimeLimit());
                runRequest.put("memoryLimit", problem.getMemoryLimit());

                // LeetCode-style function metadata
                runRequest.put("leetcodeStyle", leetcodeStyle);
                runRequest.put("functionName", problem.getFunctionName());
                runRequest.put("functionSignature", problem.getFunctionSignature());

                publishRunCodeRequest(runRequest);
                testCaseNumber++;
            }
        }

        // Wait for results with timeout
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("Timeout waiting for run code results for request {}", requestId);
            cleanup(requestId);
            return createErrorResponse("Execution timed out");
        } catch (Exception e) {
            log.error("Error waiting for run code results", e);
            cleanup(requestId);
            return createErrorResponse("Error executing code: " + e.getMessage());
        }
    }

    public void handleRunCodeResult(Map<String, Object> result) {
        String requestId = (String) result.get("requestId");
        if (requestId == null || !pendingRequests.containsKey(requestId)) {
            log.warn("Received result for unknown request: {}", requestId);
            return;
        }

        RunCodeResponse.TestCaseResult testResult = new RunCodeResponse.TestCaseResult(
                (Integer) result.get("testCaseNumber"),
                (String) result.get("input"),
                (String) result.get("expectedOutput"),
                (String) result.get("actualOutput"),
                (Boolean) result.get("passed"),
                result.get("executionTime") != null ? ((Number) result.get("executionTime")).longValue() : null,
                result.get("memoryUsed") != null ? ((Number) result.get("memoryUsed")).longValue() : null,
                (String) result.get("status"),
                (String) result.get("errorMessage")
        );

        List<RunCodeResponse.TestCaseResult> results = partialResults.get(requestId);
        results.add(testResult);

        Integer expected = expectedCounts.get(requestId);
        if (results.size() >= expected) {
            // All results received
            results.sort(Comparator.comparingInt(RunCodeResponse.TestCaseResult::testCaseNumber));

            long totalTime = results.stream()
                    .filter(r -> r.executionTime() != null)
                    .mapToLong(RunCodeResponse.TestCaseResult::executionTime)
                    .sum();

            long maxMemory = results.stream()
                    .filter(r -> r.memoryUsed() != null)
                    .mapToLong(RunCodeResponse.TestCaseResult::memoryUsed)
                    .max()
                    .orElse(0);

            boolean allPassed = results.stream().allMatch(r -> Boolean.TRUE.equals(r.passed()));
            String overallStatus = allPassed ? "ACCEPTED" : "WRONG_ANSWER";

            // Check for errors
            Optional<RunCodeResponse.TestCaseResult> errorResult = results.stream()
                    .filter(r -> r.status() != null && !r.status().equals("ACCEPTED") && !r.status().equals("WRONG_ANSWER"))
                    .findFirst();

            if (errorResult.isPresent()) {
                overallStatus = errorResult.get().status();
            }

            RunCodeResponse response = new RunCodeResponse(
                    overallStatus,
                    results,
                    totalTime,
                    maxMemory,
                    null
            );

            CompletableFuture<RunCodeResponse> future = pendingRequests.get(requestId);
            if (future != null) {
                future.complete(response);
            }

            cleanup(requestId);
        }
    }

    private void publishRunCodeRequest(Map<String, Object> request) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaObjectTemplate.send(RUN_CODE_TOPIC, request);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish run code request", ex);
                }
            });
        } catch (Exception ex) {
            log.error("Error publishing run code request", ex);
        }
    }

    private void cleanup(String requestId) {
        pendingRequests.remove(requestId);
        partialResults.remove(requestId);
        expectedCounts.remove(requestId);
    }

    private RunCodeResponse createErrorResponse(String message) {
        return new RunCodeResponse("ERROR", List.of(), 0L, 0L, message);
    }
}
