package com.userservice.repository;

import com.userservice.entity.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

    Optional<UserStatistics> findByUserId(String userId);

    @Query("SELECT us FROM UserStatistics us ORDER BY us.totalScore DESC, us.totalProblemsSolved DESC")
    List<UserStatistics> findTopByScore();

    @Query("SELECT us FROM UserStatistics us ORDER BY us.totalProblemsSolved DESC")
    List<UserStatistics> findTopBySolved();

    @Query("SELECT COUNT(us) + 1 FROM UserStatistics us WHERE us.totalScore > " +
            "(SELECT COALESCE(us2.totalScore, 0) FROM UserStatistics us2 WHERE us2.userId = :userId)")
    Integer findRankByUserId(@Param("userId") String userId);

    boolean existsByUserId(String userId);
}
