package com.codeexecutor.listener;

import com.judgeservice.dto.JudgeExecuteRequest;
import com.judgeservice.dto.JudgeResult;
import com.codeexecutor.service.CodeExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class JudgeExecuteListener {

    private static final Logger log = LoggerFactory.getLogger(JudgeExecuteListener.class);
    private static final String JUDGE_RESULTS_TOPIC = "judge.results";

    private final CodeExecutionService codeExecutionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public JudgeExecuteListener(CodeExecutionService codeExecutionService,
                                KafkaTemplate<String, Object> kafkaTemplate) {
        this.codeExecutionService = codeExecutionService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "judge.execute",
            containerFactory = "judgeExecuteKafkaListenerContainerFactory",
            groupId = "code-executor-group"
    )
    public void consumeJudgeExecuteJob(
            @Payload JudgeExecuteRequest request,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Received judge execute job: submissionId={}, testCaseId={}, language={}, topic={}, partition={}, offset={}", 
                request.getSubmissionId(), request.getTestCaseId(), request.getLanguage(), topic, partition, offset);

        try {
            // Execute the code
            JudgeResult result = codeExecutionService.executeCode(request);
            
            log.info("Execution completed: submissionId={}, testCaseId={}, verdict={}", 
                    result.getSubmissionId(), result.getTestCaseId(), result.getVerdict());

            // Publish result to judge.results topic
            kafkaTemplate.send(JUDGE_RESULTS_TOPIC, String.valueOf(result.getSubmissionId()), result);
            
            log.debug("Published result to judge.results topic for submission {} test case {}", 
                    result.getSubmissionId(), result.getTestCaseId());

        } catch (Exception ex) {
            log.error("Error processing judge execute job for submission {} test case {}", 
                    request.getSubmissionId(), request.getTestCaseId(), ex);
            
            // Publish error result
            JudgeResult errorResult = new JudgeResult(
                    request.getSubmissionId(),
                    request.getTestCaseId(),
                    "SYSTEM_ERROR",
                    "",
                    "System error: " + ex.getMessage(),
                    0L,
                    0L
            );
            
            try {
                kafkaTemplate.send(JUDGE_RESULTS_TOPIC, String.valueOf(errorResult.getSubmissionId()), errorResult);
            } catch (Exception e) {
                log.error("Failed to publish error result", e);
            }
        }
    }
}

