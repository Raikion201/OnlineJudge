package com.judgeservice.service;

import com.judgeservice.client.ProblemClient;
import com.judgeservice.client.SubmissionClient;
import com.judgeservice.client.dto.ProblemResponse;
import com.judgeservice.client.dto.SubmissionResponse;
import com.judgeservice.client.dto.SubmissionStatusUpdateRequest;
import com.judgeservice.client.dto.TestCaseResponse;
import com.judgeservice.dto.JudgeExecuteRequest;
import com.judgeservice.dto.JudgeResult;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JudgeCoordinatorService {

    private static final Logger log = LoggerFactory.getLogger(JudgeCoordinatorService.class);
    private static final String JUDGE_EXECUTE_TOPIC = "judge.execute";

    private final SubmissionClient submissionClient;
    private final ProblemClient problemClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Track pending test cases for each submission
    // Key: submissionId, Value: Set of testCaseIds that are pending
    private final Map<Long, Set<Long>> pendingTestCases = new ConcurrentHashMap<>();

    // Track results for each submission
    // Key: submissionId, Value: List of results
    private final Map<Long, List<JudgeResult>> submissionResults = new ConcurrentHashMap<>();

    public JudgeCoordinatorService(SubmissionClient submissionClient,
                                   ProblemClient problemClient,
                                   KafkaTemplate<String, Object> kafkaTemplate) {
        this.submissionClient = submissionClient;
        this.problemClient = problemClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processSubmission(Long submissionId) {
        log.info("Processing submission: {}", submissionId);

        try {
            // 1. Get submission details from SubmissionService
            SubmissionResponse submission = submissionClient.getSubmission(submissionId);
            log.debug("Retrieved submission {}: problemId={}, language={}", 
                    submissionId, submission.getProblemId(), submission.getLanguage());

            // 2. Get problem and test cases from ProblemService
            ProblemResponse problem = problemClient.getProblemById(submission.getProblemId());
            log.debug("Retrieved problem {} with {} test cases", 
                    problem.getId(), problem.getTestCases() != null ? problem.getTestCases().size() : 0);

            if (problem.getTestCases() == null || problem.getTestCases().isEmpty()) {
                log.warn("Problem {} has no test cases", problem.getId());
                updateSubmissionStatus(submissionId, "SYSTEM_ERROR", "No test cases found for problem");
                return;
            }

            // Filter out sample test cases - only judge against hidden test cases
            List<TestCaseResponse> hiddenTestCases = problem.getTestCases().stream()
                    .filter(tc -> !tc.isSample())
                    .sorted(Comparator.comparingInt(TestCaseResponse::getOrdering))
                    .toList();

            if (hiddenTestCases.isEmpty()) {
                log.warn("Problem {} has no hidden test cases", problem.getId());
                updateSubmissionStatus(submissionId, "SYSTEM_ERROR", "No hidden test cases found for problem");
                return;
            }

            // 3. Initialize tracking for this submission
            Set<Long> testCaseIds = new HashSet<>();
            for (TestCaseResponse testCase : hiddenTestCases) {
                testCaseIds.add(testCase.getId());
            }
            pendingTestCases.put(submissionId, new HashSet<>(testCaseIds));
            submissionResults.put(submissionId, new ArrayList<>());

            // 4. Update submission status to RUNNING
            updateSubmissionStatus(submissionId, "RUNNING", "Processing test cases");

            // 5. Publish jobs to judge.execute topic for each test case
            int timeLimit = problem.getTimeLimit();
            int memoryLimit = problem.getMemoryLimit();

            for (TestCaseResponse testCase : hiddenTestCases) {
                JudgeExecuteRequest executeRequest = new JudgeExecuteRequest();
                executeRequest.setSubmissionId(submissionId);
                executeRequest.setTestCaseId(testCase.getId());
                executeRequest.setCode(submission.getCode());
                executeRequest.setLanguage(submission.getLanguage());
                executeRequest.setInput(testCase.getInput());
                executeRequest.setExpectedOutput(testCase.getExpectedOutput());
                executeRequest.setTimeLimit(testCase.getTimeLimit() != null ? testCase.getTimeLimit() : timeLimit);
                executeRequest.setMemoryLimit(testCase.getMemoryLimit() != null ? testCase.getMemoryLimit() : memoryLimit);

                try {
                    kafkaTemplate.send(JUDGE_EXECUTE_TOPIC, String.valueOf(submissionId), executeRequest);
                    log.debug("Published judge job for submission {} test case {}", 
                            submissionId, testCase.getId());
                } catch (Exception ex) {
                    log.error("Failed to publish judge job for submission {} test case {}", 
                            submissionId, testCase.getId(), ex);
                }
            }

            log.info("Published {} judge jobs for submission {}", hiddenTestCases.size(), submissionId);

        } catch (FeignException.NotFound ex) {
            log.error("Submission {} or problem not found", submissionId, ex);
            updateSubmissionStatus(submissionId, "SYSTEM_ERROR", "Submission or problem not found");
        } catch (FeignException ex) {
            log.error("Error fetching submission {} or problem details", submissionId, ex);
            updateSubmissionStatus(submissionId, "SYSTEM_ERROR", "Failed to fetch submission or problem details");
        } catch (Exception ex) {
            log.error("Unexpected error processing submission {}", submissionId, ex);
            updateSubmissionStatus(submissionId, "SYSTEM_ERROR", "Unexpected error: " + ex.getMessage());
        }
    }

    public void processJudgeResult(JudgeResult result) {
        log.info("Processing judge result for submission {} test case {}: {}", 
                result.getSubmissionId(), result.getTestCaseId(), result.getVerdict());

        Long submissionId = result.getSubmissionId();
        Long testCaseId = result.getTestCaseId();

        // Add result to the list
        submissionResults.computeIfAbsent(submissionId, k -> new ArrayList<>()).add(result);

        // Remove test case from pending set
        Set<Long> pending = pendingTestCases.get(submissionId);
        if (pending != null) {
            pending.remove(testCaseId);

            // If all test cases are complete, determine final verdict
            if (pending.isEmpty()) {
                determineFinalVerdict(submissionId);
            }
        } else {
            log.warn("Received result for submission {} but no pending test cases tracked", submissionId);
        }
    }

    private void determineFinalVerdict(Long submissionId) {
        log.info("Determining final verdict for submission {}", submissionId);

        List<JudgeResult> results = submissionResults.get(submissionId);
        if (results == null || results.isEmpty()) {
            log.warn("No results found for submission {}", submissionId);
            updateSubmissionStatus(submissionId, "SYSTEM_ERROR", "No test case results found");
            return;
        }

        // Check results in order of priority
        String finalVerdict = "ACCEPTED";
        String resultMessage = null;

        for (JudgeResult result : results) {
            String verdict = result.getVerdict();
            
            // Priority order: SYSTEM_ERROR > COMPILATION_ERROR > RUNTIME_ERROR > 
            // TIME_LIMIT_EXCEEDED > MEMORY_LIMIT_EXCEEDED > WRONG_ANSWER > ACCEPTED
            if ("SYSTEM_ERROR".equals(verdict)) {
                finalVerdict = "SYSTEM_ERROR";
                resultMessage = "System error occurred during execution";
                break;
            } else if ("COMPILATION_ERROR".equals(verdict)) {
                finalVerdict = "COMPILATION_ERROR";
                resultMessage = result.getErrorMessage() != null ? result.getErrorMessage() : "Compilation failed";
                break;
            } else if ("RUNTIME_ERROR".equals(verdict)) {
                finalVerdict = "RUNTIME_ERROR";
                if (resultMessage == null) {
                    resultMessage = result.getErrorMessage() != null ? result.getErrorMessage() : "Runtime error occurred";
                }
            } else if ("TIME_LIMIT_EXCEEDED".equals(verdict)) {
                if (!"RUNTIME_ERROR".equals(finalVerdict)) {
                    finalVerdict = "TIME_LIMIT_EXCEEDED";
                    resultMessage = "Time limit exceeded";
                }
            } else if ("MEMORY_LIMIT_EXCEEDED".equals(verdict)) {
                if (!"RUNTIME_ERROR".equals(finalVerdict) && !"TIME_LIMIT_EXCEEDED".equals(finalVerdict)) {
                    finalVerdict = "MEMORY_LIMIT_EXCEEDED";
                    resultMessage = "Memory limit exceeded";
                }
            } else if ("WRONG_ANSWER".equals(verdict)) {
                if ("ACCEPTED".equals(finalVerdict)) {
                    finalVerdict = "WRONG_ANSWER";
                    resultMessage = "Wrong answer on test case " + result.getTestCaseId();
                }
            }
        }

        // Update submission with final verdict
        updateSubmissionStatus(submissionId, finalVerdict, resultMessage);

        // Clean up tracking data
        pendingTestCases.remove(submissionId);
        submissionResults.remove(submissionId);

        log.info("Final verdict for submission {}: {}", submissionId, finalVerdict);
    }

    private void updateSubmissionStatus(Long submissionId, String status, String resultMessage) {
        try {
            SubmissionStatusUpdateRequest request = new SubmissionStatusUpdateRequest();
            request.setStatus(status);
            request.setResultMessage(resultMessage);
            submissionClient.updateSubmissionStatus(submissionId, request);
            log.debug("Updated submission {} status to {}", submissionId, status);
        } catch (Exception ex) {
            log.error("Failed to update submission {} status to {}", submissionId, status, ex);
        }
    }
}

