package com.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "language_stats", indexes = {
        @Index(name = "idx_lang_user_id", columnList = "user_id"),
        @Index(name = "idx_lang_user_language", columnList = "user_id, language", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "language", nullable = false, length = 32)
    private String language;

    @Column(name = "submission_count")
    @Builder.Default
    private Integer submissionCount = 0;

    @Column(name = "accepted_count")
    @Builder.Default
    private Integer acceptedCount = 0;

    @Column(name = "problems_solved")
    @Builder.Default
    private Integer problemsSolved = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
