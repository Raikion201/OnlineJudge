package com.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_statistics", indexes = {
        @Index(name = "idx_stats_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_stats_ranking", columnList = "ranking")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 64)
    private String userId;

    @Column(name = "total_submissions")
    @Builder.Default
    private Integer totalSubmissions = 0;

    @Column(name = "accepted_submissions")
    @Builder.Default
    private Integer acceptedSubmissions = 0;

    @Column(name = "wrong_answers")
    @Builder.Default
    private Integer wrongAnswers = 0;

    @Column(name = "time_limit_exceeded")
    @Builder.Default
    private Integer timeLimitExceeded = 0;

    @Column(name = "runtime_errors")
    @Builder.Default
    private Integer runtimeErrors = 0;

    @Column(name = "compilation_errors")
    @Builder.Default
    private Integer compilationErrors = 0;

    @Column(name = "easy_solved")
    @Builder.Default
    private Integer easySolved = 0;

    @Column(name = "medium_solved")
    @Builder.Default
    private Integer mediumSolved = 0;

    @Column(name = "hard_solved")
    @Builder.Default
    private Integer hardSolved = 0;

    @Column(name = "total_problems_solved")
    @Builder.Default
    private Integer totalProblemsSolved = 0;

    @Column(name = "total_score")
    @Builder.Default
    private Double totalScore = 0.0;

    @Column(name = "acceptance_rate")
    @Builder.Default
    private Double acceptanceRate = 0.0;

    @Column(name = "ranking")
    @Builder.Default
    private Integer ranking = 0;

    @Column(name = "contribution_points")
    @Builder.Default
    private Integer contributionPoints = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
