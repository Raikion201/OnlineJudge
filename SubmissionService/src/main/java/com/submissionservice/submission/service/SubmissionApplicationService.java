package com.submissionservice.submission.service;

import com.submissionservice.submission.client.ProblemClient;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SubmissionApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionApplicationService.class);
    private static final String SUBMISSION_JOBS_TOPIC = "submission.jobs";

    private final SubmissionRepository submissionRepository;
    private final KafkaTemplate<String, Long> kafkaTemplate;
    private final ProblemClient problemClient;
    private final com.submissionservice.submission.client.UserClient userClient;

    public SubmissionApplicationService(SubmissionRepository submissionRepository,
                                        KafkaTemplate<String, Long> kafkaTemplate,
                                        ProblemClient problemClient,
                                        com.submissionservice.submission.client.UserClient userClient) {
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


        Submission submission = new Submission();
        submission.setProblemId(request.problemId());
        submission.setUserId(userId);
        submission.setLanguage(request.language());
        submission.setSourceCode(request.code());
        submission.setStatus(SubmissionStatus.PENDING);

        Submission saved = submissionRepository.save(submission);

        registerKafkaPublishAfterCommit(saved.getId());

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
        if (request.score() != null) {
            submission.setScore(request.score());
        }
        if (request.executionTime() != null) {
            submission.setExecutionTime(request.executionTime());
        }
        if (request.memoryUsage() != null) {
            submission.setMemoryUsage(request.memoryUsage());
        }
        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<com.submissionservice.submission.dto.LeaderboardEntryDTO> getLeaderboard(Long problemId) {
        List<Submission> submissions = submissionRepository.findLeaderboardByProblemId(problemId, 10);

        return submissions.stream().map(s -> {
            String username = s.getUserId(); // Default to userId
            try {
                com.submissionservice.submission.client.dto.UserResponse user = userClient.getUserByKeycloakId(s.getUserId());
                if (user != null && user.getUsername() != null) {
                    username = user.getUsername();
                }
            } catch (Exception e) {
                log.debug("User info not found for userId: {}, using userId as display name", s.getUserId());
            }

            return new com.submissionservice.submission.dto.LeaderboardEntryDTO(
                    s.getUserId(),
                    username,
                    s.getScore(),
                    s.getExecutionTime(),
                    s.getLanguage(),
                    s.getCreatedAt()
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<com.submissionservice.submission.dto.GlobalLeaderboardDTO> getGlobalLeaderboard() {
        List<com.submissionservice.submission.repository.GlobalLeaderboardProjection> projections = submissionRepository.findGlobalLeaderboard();
        
        java.util.concurrent.atomic.AtomicInteger rankCounter = new java.util.concurrent.atomic.AtomicInteger(1);

        return projections.stream().map(p -> {
            String username = p.getUserId();
            try {
                com.submissionservice.submission.client.dto.UserResponse user = userClient.getUserByKeycloakId(p.getUserId());
                if (user != null && user.getUsername() != null) {
                    username = user.getUsername();
                }
            } catch (Exception e) {
                log.debug("User info not found for userId: {}, using userId as display name", p.getUserId());
            }
            
            com.submissionservice.submission.dto.GlobalLeaderboardDTO dto = new com.submissionservice.submission.dto.GlobalLeaderboardDTO(
                    p.getUserId(),
                    p.getTotalScore(),
                    p.getTotalAccepted(),
                    p.getTotalSubmissions(),
                    p.getLastSubmission()
            );
            dto.setUsername(username);
            dto.setRank(rankCounter.getAndIncrement());
            return dto;
        }).toList();
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

    private void registerKafkaPublishAfterCommit(Long submissionId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishSubmissionJob(submissionId);
                }
            });
        } else {
            publishSubmissionJob(submissionId);
        }
    }

    private void publishSubmissionJob(Long submissionId) {
        try {
            CompletableFuture<SendResult<String, Long>> future =
                    kafkaTemplate.send(SUBMISSION_JOBS_TOPIC, submissionId);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published submission {} to topic {}",
                            submissionId, SUBMISSION_JOBS_TOPIC);
                } else {
                    log.error("Failed to publish submission {} to topic {}",
                            submissionId, SUBMISSION_JOBS_TOPIC, ex);
                }
            });
        } catch (Exception ex) {
            log.error("Error publishing submission {} to Kafka topic {}",
                    submissionId, SUBMISSION_JOBS_TOPIC, ex);
        }
    }
}

