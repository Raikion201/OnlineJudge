package com.submissionservice.submission.listener;

import com.submissionservice.submission.service.RunCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RunCodeResultsListener {

    private static final Logger log = LoggerFactory.getLogger(RunCodeResultsListener.class);
    private final RunCodeService runCodeService;

    public RunCodeResultsListener(RunCodeService runCodeService) {
        this.runCodeService = runCodeService;
    }

    @KafkaListener(topics = "run.code.results", groupId = "submission-service-run-results",
            containerFactory = "runCodeResultsKafkaListenerContainerFactory")
    public void onRunCodeResult(Map<String, Object> result) {
        log.info("Received run code result: requestId={}, testCaseNumber={}, status={}",
                result.get("requestId"), result.get("testCaseNumber"), result.get("status"));
        try {
            runCodeService.handleRunCodeResult(result);
        } catch (Exception e) {
            log.error("Error processing run code result: {}", e.getMessage(), e);
        }
    }
}
