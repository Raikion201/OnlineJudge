package com.codeexecutor.listener;

import com.codeexecutor.service.CodeExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RunCodeListener {

    private static final Logger log = LoggerFactory.getLogger(RunCodeListener.class);
    private static final String RUN_CODE_RESULTS_TOPIC = "run.code.results";

    private final CodeExecutionService codeExecutionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RunCodeListener(CodeExecutionService codeExecutionService,
                           KafkaTemplate<String, Object> kafkaTemplate) {
        this.codeExecutionService = codeExecutionService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "run.code", groupId = "code-executor-run-group")
    public void consumeRunCodeJob(Map<String, Object> request) {
        String requestId = (String) request.get("requestId");
        Integer testCaseNumber = (Integer) request.get("testCaseNumber");

        log.info("Received run code job: requestId={}, testCaseNumber={}", requestId, testCaseNumber);

        Map<String, Object> result = new HashMap<>();
        result.put("requestId", requestId);
        result.put("testCaseNumber", testCaseNumber);
        result.put("input", request.get("input"));
        result.put("expectedOutput", request.get("expectedOutput"));

        try {
            String code = (String) request.get("code");
            String language = (String) request.get("language");
            String input = (String) request.get("input");
            String expectedOutput = (String) request.get("expectedOutput");
            int timeLimit = request.get("timeLimit") != null ? ((Number) request.get("timeLimit")).intValue() : 1000;
            int memoryLimit = request.get("memoryLimit") != null ? ((Number) request.get("memoryLimit")).intValue() : 256;

            // LeetCode-style function metadata
            String functionName = (String) request.get("functionName");
            String functionSignature = (String) request.get("functionSignature");
            boolean leetcodeStyle = request.get("leetcodeStyle") != null && (Boolean) request.get("leetcodeStyle");

            log.debug("Run code input: leetcodeStyle={}, functionName={}, functionSignature={}, input={}",
                    leetcodeStyle, functionName, functionSignature, input);

            // Execute the code using the existing service
            CodeExecutionService.ExecutionResult execResult = codeExecutionService.executeForRunCode(
                    code, language, input, timeLimit, memoryLimit,
                    leetcodeStyle, functionName, functionSignature
            );

            log.debug("Run code output: status={}, output={}, error={}",
                    execResult.status(), execResult.output(), execResult.errorMessage());

            result.put("actualOutput", execResult.output());
            result.put("executionTime", execResult.executionTime());
            result.put("memoryUsed", execResult.memoryUsage());
            result.put("status", execResult.status());
            result.put("errorMessage", execResult.errorMessage());

            // Check if output matches expected (if expectedOutput is provided)
            if (expectedOutput != null && !expectedOutput.isEmpty()) {
                boolean passed = normalizeOutput(execResult.output()).equals(normalizeOutput(expectedOutput));
                result.put("passed", passed);
                if (!passed && "ACCEPTED".equals(execResult.status())) {
                    result.put("status", "WRONG_ANSWER");
                }
            } else {
                result.put("passed", true); // Custom input, no comparison
            }

            log.info("Run code completed: requestId={}, testCaseNumber={}, status={}, errorMessage={}",
                    requestId, testCaseNumber, result.get("status"), result.get("errorMessage"));

        } catch (Exception ex) {
            log.error("Error executing run code job", ex);
            result.put("actualOutput", "");
            result.put("executionTime", 0L);
            result.put("memoryUsed", 0L);
            result.put("status", "SYSTEM_ERROR");
            result.put("errorMessage", ex.getMessage());
            result.put("passed", false);
        }

        // Publish result
        try {
            log.info("Publishing run code result: requestId={}, testCaseNumber={}, status={}",
                    requestId, testCaseNumber, result.get("status"));
            kafkaTemplate.send(RUN_CODE_RESULTS_TOPIC, requestId, result)
                    .whenComplete((sendResult, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish run code result to Kafka", ex);
                        } else {
                            log.info("Successfully published run code result to Kafka: requestId={}", requestId);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to publish run code result", e);
        }
    }

    private String normalizeOutput(String output) {
        if (output == null) return "";
        return output.trim().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
    }
}
