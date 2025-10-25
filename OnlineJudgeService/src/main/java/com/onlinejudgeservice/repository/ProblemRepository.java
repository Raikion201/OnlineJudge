package com.onlinejudgeservice.repository;

import com.onlinejudgeservice.entity.Problem;
import com.onlinejudgeservice.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByTags_SlugIn(List<String> slugs);

    @Query("SELECT DISTINCT p FROM Problem p " +
            "LEFT JOIN p.tags t " +
            "WHERE (:keyword IS NULL OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:tags IS NULL OR t.slug IN :tags) " +
            "AND (:difficulty IS NULL OR p.difficulty = :difficulty)")
    Page<Problem> searchProblems(@Param("keyword") String keyword,
                                 @Param("tags") List<String> tags,
                                 @Param("difficulty") Difficulty difficulty,
                                 Pageable pageable);
}