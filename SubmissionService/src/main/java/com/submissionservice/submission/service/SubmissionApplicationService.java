package com.submissionservice.submission.service;

import com.submissionservice.submission.client.ProblemClient;
import com.submissionservice.submission.client.UserClient;
import com.submissionservice.submission.dto.SubmissionRequest;
import com.submissionservice.submission.dto.SubmissionResponse;
import com.submissionservice.submission.dto.SubmissionStatusUpdateRequest;
import com.submissionservice.submission.exception.BadRequestException;
import com.submissionservice.submission.exception.SubmissionNotFoundException;
import com.submissionservice.submission.model.Submission;
import com.submissionservice.submission.model.SubmissionStatus;
import com.submissionservice.submission.repository.SubmissionRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SubmissionApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionApplicationService.class);
    private static final String SUBMISSION_JOBS_TOPIC = "submission.jobs";

    private final SubmissionRepository submissionRepository;
    private final KafkaTemplate<String, Long> kafkaTemplate;
    private final ProblemClient problemClient;
    private final UserClient userClient;

    public SubmissionApplicationService(SubmissionRepository submissionRepository, 
                                        KafkaTemplate<String, Long> kafkaTemplate,
                                        ProblemClient problemClient,
                                        UserClient userClient) {
        this.submissionRepository = submissionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.problemClient = problemClient;
        this.userClient = userClient;
    }

    @Transactional
    public SubmissionResponse createSubmission(SubmissionRequest request, String userId) {
        // Validate problem exists
        try {
            problemClient.getProblemById(request.problemId());
            log.debug("Problem {} validated", request.problemId());
        } catch (FeignException.NotFound ex) {
            log.warn("Problem {} not found", request.problemId());
            throw new BadRequestException("Problem not found with id: " + request.problemId());
        } catch (FeignException ex) {
            log.error("Error validating problem {}: {}", request.problemId(), ex.getMessage());
            throw new BadRequestException("Failed to validate problem: " + ex.getMessage());
        }

        // Validate user exists
        try {
            userClient.getUserByKeycloakId(userId);
            log.debug("User {} validated", userId);
        } catch (FeignException.NotFound ex) {
            log.warn("User {} not found", userId);
            throw new BadRequestException("User not found with keycloakId: " + userId);
        } catch (FeignException ex) {
            log.error("Error validating user {}: {}", userId, ex.getMessage());
            throw new BadRequestException("Failed to validate user: " + ex.getMessage());
        }

        Submission submission = new Submission();
        submission.setProblemId(request.problemId());
        submission.setUserId(userId);
        submission.setLanguage(request.language());
        submission.setSourceCode(request.code());
        submission.setStatus(SubmissionStatus.PENDING);

        Submission saved = submissionRepository.save(submission);

        // Publish submission_id to submission.jobs Kafka topic
        try {
            CompletableFuture<SendResult<String, Long>> future = kafkaTemplate.send(SUBMISSION_JOBS_TOPIC, saved.getId());
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published submission {} to topic {}", 
                            saved.getId(), SUBMISSION_JOBS_TOPIC);
                } else {
                    log.error("Failed to publish submission {} to topic {}", 
                            saved.getId(), SUBMISSION_JOBS_TOPIC, ex);
                }
            });
        } catch (Exception ex) {
            log.error("Error publishing submission {} to Kafka topic {}", 
                    saved.getId(), SUBMISSION_JOBS_TOPIC, ex);
            // Note: We don't change status here as per requirements - status remains PENDING
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionForUser(Long submissionId, String userId) {
        Submission submission = submissionRepository.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new SubmissionNotFoundException(submissionId));
        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getUserSubmissions(String userId) {
        return submissionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionInternal(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException(submissionId));
        return toResponse(submission);
    }

    @Transactional
    public SubmissionResponse updateSubmissionStatus(Long submissionId, SubmissionStatusUpdateRequest request) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException(submissionId));
        submission.setStatus(request.status());
        submission.setResultMessage(request.resultMessage());
        return toResponse(submission);
    }

    private SubmissionResponse toResponse(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getProblemId(),
                submission.getUserId(),
                submission.getLanguage(),
                submission.getSourceCode(),
                submission.getStatus(),
                submission.getResultMessage(),
                submission.getCreatedAt(),
                submission.getUpdatedAt()
        );
    }
}

