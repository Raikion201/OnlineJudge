package com.problemservice.repository;

import com.problemservice.entity.ProblemExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemExampleRepository extends JpaRepository<ProblemExample, Long> {
    List<ProblemExample> findByProblemIdOrderByOrderingAsc(Long problemId);
}
