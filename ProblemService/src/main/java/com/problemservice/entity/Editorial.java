package com.problemservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "editorials", indexes = {
        @Index(name = "idx_editorial_problem_id", columnList = "problem_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Editorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false, unique = true)
    private Long problemId;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "approach", columnDefinition = "TEXT")
    private String approach;

    @Column(name = "time_complexity", length = 100)
    private String timeComplexity;

    @Column(name = "space_complexity", length = 100)
    private String spaceComplexity;

    @Column(name = "solution_code", columnDefinition = "LONGTEXT")
    private String solutionCode;

    @Column(name = "solution_language", length = 32)
    private String solutionLanguage;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "author_id", length = 64)
    private String authorId;

    @Column(name = "author_name", length = 100)
    private String authorName;

    @Column(name = "is_premium")
    @Builder.Default
    private Boolean isPremium = false;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "helpful_count")
    @Builder.Default
    private Integer helpfulCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
