package com.userservice.repository;

import com.userservice.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<Bookmark> findByUserId(String userId, Pageable pageable);

    Optional<Bookmark> findByUserIdAndProblemId(String userId, Long problemId);

    boolean existsByUserIdAndProblemId(String userId, Long problemId);

    void deleteByUserIdAndProblemId(String userId, Long problemId);

    long countByUserId(String userId);
}
