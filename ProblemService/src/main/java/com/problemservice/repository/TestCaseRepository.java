package com.problemservice.repository;

import com.problemservice.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByProblemIdOrderByOrderingAsc(Long problemId);
    List<TestCase> findByProblemIdAndIsSampleTrueOrderByOrderingAsc(Long problemId);
}
