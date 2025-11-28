package com.problemservice.entity;

import com.problemservice.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "problems", indexes = {
    // list problem by difficulty and is_public
    @Index(name = "idx_is_public_difficulty", columnList = "is_public,difficulty"),
    // list problem by is_public and created_at sorted descending
    @Index(name = "idx_public_created_at", columnList = "is_public, created_at DESC"),
    // list problem by is_public and created_at
    @Index(name = "idx_public_created_at", columnList = "is_public,created_at"),
    // list problem by title
    @Index(name = "idx_title", columnList = "title")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")

    private String title;
    @Column(name = "description")

    private String description;
    @Column(name = "input_format")

    private String input_format;
    @Column(name = "output_format")

    private String output_format;
    @Column(name = "constraints")

    private String constraints;
    @Column(name = "difficulty")

    private Difficulty difficulty;
    @Column(name = "time_limit")

    @Builder.Default
    private int time_limit = 1000;
    @Column(name = "memory_limit")

    @Builder.Default
    private int memory_limit = 256;
    @Column(name = "created_by")

    private Long createdBy;
    @Column(name = "is_public")

    @Builder.Default
    private boolean isPublic = false;
    @Column(name = "acceptance_rate")

    @Builder.Default
    private double acceptanceRate = 0.00;
    @Column(name = "total_submissions")

    @Builder.Default
    private int totalSubmissions = 0;
    @Column(name = "total_accepted")

    @Builder.Default
    private int totalAccepted = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private java.time.LocalDateTime updatedAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TestCase> testCases = new HashSet<>();
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProblemExample> examples = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "problem_tags",
            joinColumns = @JoinColumn(name = "problem_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();
}
