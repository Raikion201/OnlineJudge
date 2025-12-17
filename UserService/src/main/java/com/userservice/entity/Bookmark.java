package com.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookmarks", indexes = {
        @Index(name = "idx_bookmark_user_id", columnList = "user_id"),
        @Index(name = "idx_bookmark_user_problem", columnList = "user_id, problem_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "problem_title", length = 255)
    private String problemTitle;

    @Column(name = "difficulty", length = 20)
    private String difficulty;

    @Column(name = "note", length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
