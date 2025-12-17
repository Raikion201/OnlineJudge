package com.problemservice.repository;

import com.problemservice.entity.Editorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EditorialRepository extends JpaRepository<Editorial, Long> {

    Optional<Editorial> findByProblemId(Long problemId);

    boolean existsByProblemId(Long problemId);

    void deleteByProblemId(Long problemId);

    @Modifying
    @Query("UPDATE Editorial e SET e.viewCount = e.viewCount + 1 WHERE e.problemId = :problemId")
    void incrementViewCount(@Param("problemId") Long problemId);

    @Modifying
    @Query("UPDATE Editorial e SET e.helpfulCount = e.helpfulCount + 1 WHERE e.problemId = :problemId")
    void incrementHelpfulCount(@Param("problemId") Long problemId);
}
