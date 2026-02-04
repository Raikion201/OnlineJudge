package com.judgeservice.listener;

import com.judgeservice.service.JudgeCoordinatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class SubmissionJobsListener {

    private static final Logger log = LoggerFactory.getLogger(SubmissionJobsListener.class);

    private final JudgeCoordinatorService judgeCoordinatorService;

    public SubmissionJobsListener(JudgeCoordinatorService judgeCoordinatorService) {
        this.judgeCoordinatorService = judgeCoordinatorService;
    }

    @KafkaListener(
            topics = "submission.jobs",
            containerFactory = "submissionJobsKafkaListenerContainerFactory",
            groupId = "judge-service-group"
    )
    public void consumeSubmissionJob(
            @Payload Long submissionId,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Received submission job: submissionId={}, topic={}, partition={}, offset={}", 
                submissionId, topic, partition, offset);

        try {
            judgeCoordinatorService.processSubmission(submissionId);
        } catch (Exception ex) {
            log.error("Error processing submission job: {}", submissionId, ex);
        }
    }
}

