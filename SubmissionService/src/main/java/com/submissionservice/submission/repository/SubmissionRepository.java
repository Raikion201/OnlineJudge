package com.submissionservice.submission.repository;

import com.submissionservice.submission.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByIdAndUserId(Long id, String userId);

    List<Submission> findAllByUserIdOrderByCreatedAtDesc(String userId);
}

