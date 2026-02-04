package com.submissionservice.submission.repository;

import com.submissionservice.submission.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByIdAndUserId(Long id, String userId);

    List<Submission> findAllByUserIdOrderByCreatedAtDesc(String userId);

    @Query(value = """
            SELECT * FROM (
                SELECT *, ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY score DESC, execution_time ASC) as rn
                FROM submissions
                WHERE problem_id = :problemId
            ) t WHERE rn = 1
            ORDER BY score DESC, execution_time ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Submission> findLeaderboardByProblemId(@Param("problemId") Long problemId, @Param("limit") int limit);

    @Query(value = """
            SELECT
                t1.user_id as userId,
                t1.total_score as totalScore,
                t2.total_accepted as totalAccepted,
                t2.total_submissions as totalSubmissions,
                t2.last_submission as lastSubmission
            FROM
                (SELECT user_id, SUM(max_score) as total_score
                 FROM (SELECT user_id, problem_id, MAX(score) as max_score FROM submissions GROUP BY user_id, problem_id) as sub
                 GROUP BY user_id) as t1
            JOIN
                (SELECT user_id,
                        COUNT(DISTINCT CASE WHEN status = 'ACCEPTED' THEN problem_id END) as total_accepted,
                        COUNT(*) as total_submissions,
                        MAX(created_at) as last_submission
                 FROM submissions
                 GROUP BY user_id) as t2
            ON t1.user_id = t2.user_id
            ORDER BY t1.total_score DESC, t2.last_submission DESC
            """, nativeQuery = true)
    List<GlobalLeaderboardProjection> findGlobalLeaderboard();
}

