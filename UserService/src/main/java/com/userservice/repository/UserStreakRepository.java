package com.userservice.repository;

import com.userservice.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

    Optional<UserStreak> findByUserId(String userId);

    @Query("SELECT us FROM UserStreak us ORDER BY us.currentStreak DESC")
    List<UserStreak> findTopStreaks();

    @Query("SELECT us FROM UserStreak us ORDER BY us.maxStreak DESC")
    List<UserStreak> findTopMaxStreaks();
}
