package com.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_activities", indexes = {
        @Index(name = "idx_user_date", columnList = "user_id, activity_date"),
        @Index(name = "idx_activity_date", columnList = "activity_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "submission_count")
    @Builder.Default
    private Integer submissionCount = 0;

    @Column(name = "accepted_count")
    @Builder.Default
    private Integer acceptedCount = 0;

    @Column(name = "problems_attempted")
    @Builder.Default
    private Integer problemsAttempted = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
