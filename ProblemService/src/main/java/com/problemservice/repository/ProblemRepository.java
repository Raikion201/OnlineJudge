package com.problemservice.repository;

import com.problemservice.entity.Problem;
import com.problemservice.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findDistinctByTagsIn(Collection<Tag> tags);
}
