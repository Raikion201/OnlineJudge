package com.userservice.repository;

import com.userservice.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    Optional<UserActivity> findByUserIdAndActivityDate(String userId, LocalDate activityDate);

    List<UserActivity> findByUserIdAndActivityDateBetweenOrderByActivityDateAsc(
            String userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.userId = :userId " +
            "AND ua.activityDate >= :startDate ORDER BY ua.activityDate ASC")
    List<UserActivity> findUserActivityFromDate(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate);

    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.userId = :userId " +
            "AND ua.submissionCount > 0 AND ua.activityDate BETWEEN :startDate AND :endDate")
    Long countActiveDays(@Param("userId") String userId,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate);
}
