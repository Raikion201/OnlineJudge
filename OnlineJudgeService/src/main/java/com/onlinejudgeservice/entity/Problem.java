package com.onlinejudgeservice.entity;

import com.onlinejudgeservice.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "problems")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Service
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
    @Column(name = "createdAt", updatable = false)
    private java.time.LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updateAt")
    private java.time.LocalDateTime updatedAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TestCase> testCases = new HashSet<>();
}
