package com.problemservice.repository;

import com.problemservice.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Fetch top-level comments (parent is null) for a problem
    Page<Comment> findByProblemIdAndParentIsNullOrderByCreatedAtDesc(Long problemId, Pageable pageable);
    
    // Fetch replies for a specific comment
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
}
