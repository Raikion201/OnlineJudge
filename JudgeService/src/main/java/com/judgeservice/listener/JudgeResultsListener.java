package com.judgeservice.listener;

import com.judgeservice.dto.JudgeResult;
import com.judgeservice.service.JudgeCoordinatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class JudgeResultsListener {

    private static final Logger log = LoggerFactory.getLogger(JudgeResultsListener.class);

    private final JudgeCoordinatorService judgeCoordinatorService;

    public JudgeResultsListener(JudgeCoordinatorService judgeCoordinatorService) {
        this.judgeCoordinatorService = judgeCoordinatorService;
    }

    @KafkaListener(
            topics = "judge.results",
            containerFactory = "judgeResultsKafkaListenerContainerFactory",
            groupId = "judge-service-results-group"
    )
    public void consumeJudgeResult(
            @Payload JudgeResult result,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Received judge result: submissionId={}, testCaseId={}, verdict={}, topic={}, partition={}, offset={}", 
                result.getSubmissionId(), result.getTestCaseId(), result.getVerdict(), topic, partition, offset);

        try {
            judgeCoordinatorService.processJudgeResult(result);
        } catch (Exception ex) {
            log.error("Error processing judge result for submission {} test case {}", 
                    result.getSubmissionId(), result.getTestCaseId(), ex);
        }
    }
}

