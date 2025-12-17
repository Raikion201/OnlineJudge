package com.userservice.repository;

import com.userservice.entity.LanguageStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageStatsRepository extends JpaRepository<LanguageStats, Long> {

    List<LanguageStats> findByUserIdOrderBySubmissionCountDesc(String userId);

    Optional<LanguageStats> findByUserIdAndLanguage(String userId, String language);

    @Query("SELECT ls.language, SUM(ls.submissionCount) as total FROM LanguageStats ls " +
            "GROUP BY ls.language ORDER BY total DESC")
    List<Object[]> findGlobalLanguageDistribution();

    @Query("SELECT ls FROM LanguageStats ls WHERE ls.userId = :userId ORDER BY ls.submissionCount DESC")
    List<LanguageStats> findUserLanguageStats(@Param("userId") String userId);
}
